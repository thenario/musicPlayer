package com.kyf.mp.server.utils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kyf.mp.server.modules.song.entity.Songs;
import com.kyf.mp.server.modules.user.entity.Users;
import com.kyf.mp.server.modules.user.mapper.UsersMapper;
import com.kyf.mp.server.modules.song.mapper.SongsMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.images.Artwork;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Profile("!test")
@ConditionalOnProperty(name = "song.scanner.enabled", havingValue = "true")
@Slf4j
@RequiredArgsConstructor
public class SongScannerTask {

    @Value("${file.upload.song-path}")
    private String songPath;
    @Value("${file.upload.song-cover-path}")
    private String songCoverPath;
    @Value("${file.static.song-url}")
    private String songUrlPrefix;
    @Value("${file.static.song-cover-url}")
    private String songCoverUrlPrefix;
    @Value("${song.scanner.uploader-id}")
    private Long scannerUploaderId;
    @Value("${song.scanner.remove-missing:false}")
    private boolean removeMissingSongs;
    private final SongsMapper songsMapper;
    private final UsersMapper usersMapper;

    @Async // 异步执行，不影响服务器启动速度
    @EventListener(ApplicationReadyEvent.class)
    public void runTask() {
        if (!isScannerConfigured()) {
            return;
        }
        Users scannerUploader = usersMapper.selectById(scannerUploaderId);
        if (scannerUploader == null) {
            log.error("Song scanner uploader does not exist, userId={}", scannerUploaderId);
            return;
        }
        log.info("Starting song scan...");
        long start = System.currentTimeMillis();

        try {
            File[] files = scanSongFiles();
            if (files == null) {
                return;
            }

            ScanResult result = compareWithDatabase(files);
            removeMissingSongs(result.idsToDelete());
            int addedCount = addNewSongs(result.filesToAdd(), scannerUploader);
            logCompletion(start, addedCount, result.idsToDelete().size());
        } catch (Exception e) {
            log.error("Song scan task failed: ", e);
        }
    }

    private boolean isScannerConfigured() {
        if (scannerUploaderId == null || scannerUploaderId <= 0) {
            log.error("song.scanner.uploader-id must be a valid user ID");
            return false;
        }
        return true;
    }

    private File[] scanSongFiles() {
        File[] files = new File(songPath).listFiles((d, name) -> isSupportedSong(name));
        if (files == null) {
            log.warn("Song directory does not exist or is not readable, path={}", songPath);
        }
        return files;
    }

    private boolean isSupportedSong(String name) {
        String extension = name.toLowerCase();
        return extension.endsWith(".mp3") || extension.endsWith(".flac")
                || extension.endsWith(".wav") || extension.endsWith(".m4a");
    }

    private ScanResult compareWithDatabase(File[] files) {
        Set<String> localUrls = buildLocalUrls(files);
        List<Songs> dbSongs = songsMapper.selectList(new QueryWrapper<Songs>()
                .select("song_id", "song_url").eq("uploader_id", scannerUploaderId));
        Set<String> dbUrls = dbSongs.stream().map(Songs::getSongUrl).collect(Collectors.toSet());
        List<Long> idsToDelete = dbSongs.stream()
                .filter(song -> !localUrls.contains(song.getSongUrl()))
                .map(Songs::getSongId)
                .collect(Collectors.toList());
        return new ScanResult(idsToDelete, findNewFiles(files, dbUrls));
    }

    private Set<String> buildLocalUrls(File[] files) {
        Set<String> localUrls = new HashSet<>();
        for (File file : files) {
            localUrls.add(songUrlPrefix + file.getName());
        }
        return localUrls;
    }

    private List<File> findNewFiles(File[] files, Set<String> dbUrls) {
        List<File> filesToAdd = new ArrayList<>();
        for (File file : files) {
            if (!dbUrls.contains(songUrlPrefix + file.getName())) {
                filesToAdd.add(file);
            }
        }
        return filesToAdd;
    }

    private void removeMissingSongs(List<Long> idsToDelete) {
        if (idsToDelete.isEmpty()) {
            return;
        }
        if (removeMissingSongs) {
            log.info("Removing {} missing song records", idsToDelete.size());
            songsMapper.deleteBatchIds(idsToDelete);
            return;
        }
        log.warn("Found {} missing song records; removal is disabled", idsToDelete.size());
    }

    private int addNewSongs(List<File> filesToAdd, Users scannerUploader) {
        int addedCount = 0;
        for (File file : filesToAdd) {
            if (insertNewSong(file, scannerUploader)) {
                addedCount++;
            }
        }
        return addedCount;
    }

    private void logCompletion(long start, int addedCount, int removedCount) {
        log.info("Song scan completed: {}ms, added: {}, removed: {}",
                (System.currentTimeMillis() - start), addedCount,
                removeMissingSongs ? removedCount : 0);
    }

    private record ScanResult(List<Long> idsToDelete, List<File> filesToAdd) {
    }

    private boolean insertNewSong(File file, Users scannerUploader) {
        try {
            log.info("🎵 正在录入新歌�? {}", file.getName());
            AudioFile f = AudioFileIO.read(file);
            Tag tag = f.getTag();
            AudioHeader header = f.getAudioHeader();

            Songs song = new Songs();
            // 快速指纹：文件�?大小的哈希，代替全量MD5，速度极快
            song.setFileMd5(calculateMd5(file));

            song.setUploaderId(scannerUploaderId);
            song.setUploaderName(scannerUploader.getUserName());

            String title = (tag != null) ? tag.getFirst(FieldKey.TITLE) : "";
            song.setSongTitle(StringUtils.hasText(title) ? title : file.getName());
            song.setArtist((tag != null) ? tag.getFirst(FieldKey.ARTIST) : "未知艺术家");
            song.setAlbum((tag != null) ? tag.getFirst(FieldKey.ALBUM) : "未知专辑");

            song.setDuration(header.getTrackLength());
            song.setBitrate((int) header.getBitRateAsNumber());
            song.setFileSize(file.length());
            song.setFileFormat(StringUtils.getFilenameExtension(file.getName()));
            song.setSongUrl(songUrlPrefix + file.getName());
            song.setDateAdded(LocalDateTime.now());

            // 封面处理
            song.setSongCoverUrl(extractArtwork(tag));

            songsMapper.insert(song);
            return true;
        } catch (Exception e) {
            log.error("解析失败 {}: {}", file.getName(), e.getMessage());
            return false;
        }
    }

    private String calculateMd5(File file) throws IOException {
        try (InputStream inputStream = new FileInputStream(file)) {
            return DigestUtils.md5DigestAsHex(inputStream);
        }
    }

    private String extractArtwork(Tag tag) {
        if (tag == null)
            return null;
        try {
            Artwork artwork = tag.getFirstArtwork();
            if (artwork != null) {
                byte[] data = artwork.getBinaryData();
                String coverName = "auto-" + UUID.randomUUID().toString().substring(0, 8) + ".jpg";
                File coverFile = new File(songCoverPath, coverName);
                if (!coverFile.getParentFile().exists()) {
                    boolean created = coverFile.getParentFile().mkdirs();
                    log.info("创建封面目录: {}, 结果: {}", coverFile.getParentFile().getAbsolutePath(), created);
                }
                try (FileOutputStream fos = new FileOutputStream(coverFile)) {
                    fos.write(data);
                }
                return songCoverUrlPrefix + coverName;
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}