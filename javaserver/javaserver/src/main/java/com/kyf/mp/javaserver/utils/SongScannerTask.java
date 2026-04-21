package com.kyf.mp.javaserver.utils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kyf.mp.javaserver.modules.SongModule.entity.Songs;
import com.kyf.mp.javaserver.modules.SongModule.mapper.SongsMapper;
import lombok.extern.slf4j.Slf4j;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.images.Artwork;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Slf4j
public class SongScannerTask {

    @Value("${file.upload.song-path}")
    private String songPath;
    @Value("${file.upload.song-cover-path}")
    private String songCoverPath;
    @Value("${file.static.song-url}")
    private String songUrlPrefix;
    @Value("${file.static.song-cover-url}")
    private String songCoverUrlPrefix;

    @Autowired
    private SongsMapper songsMapper;

    @Async // 异步执行，不影响服务器启动速度
    @EventListener(ApplicationReadyEvent.class)
    public void runTask() {
        log.info("🚀 启动极速双向同步任务...");
        long start = System.currentTimeMillis();

        try {
            // 1. 扫描本地文件夹，获取所有本地文件的 URL 集合
            File dir = new File(songPath);
            File[] files = dir.listFiles((d, name) -> {
                String ext = name.toLowerCase();
                return ext.endsWith(".mp3") || ext.endsWith(".flac") || ext.endsWith(".wav") || ext.endsWith(".m4a");
            });

            Set<String> localUrls = new HashSet<>();
            if (files != null) {
                for (File f : files) {
                    localUrls.add(songUrlPrefix + f.getName());
                }
            }

            // 2. 一次性读取数据库中所有歌曲（只需要 ID 和 URL 即可）
            List<Songs> dbSongs = songsMapper.selectList(new QueryWrapper<Songs>().select("song_id", "song_url"));

            Set<String> dbUrls = dbSongs.stream()
                    .map(Songs::getSongUrl)
                    .collect(Collectors.toSet());

            // 3. 找出需要删除的记录：数据库里有，但是本地 URL 集合里没有
            List<Integer> idsToDelete = dbSongs.stream()
                    .filter(s -> !localUrls.contains(s.getSongUrl()))
                    .map(Songs::getSongId)
                    .collect(Collectors.toList());

            // 4. 找出需要新增的记录：本地有，但数据库 URL 集合里没有
            List<File> filesToAdd = new ArrayList<>();
            if (files != null) {
                for (File file : files) {
                    String currentUrl = songUrlPrefix + file.getName();
                    if (!dbUrls.contains(currentUrl)) {
                        filesToAdd.add(file);
                    }
                }
            }

            // 5. 执行清理（删除死链）
            if (!idsToDelete.isEmpty()) {
                log.info("🗑️ 检测到本地文件已删除，正在清理数据库死链，共 {} 条...", idsToDelete.size());
                // 使用 MyBatis-Plus 的批量删除，速度极快
                songsMapper.deleteBatchIds(idsToDelete);
            }

            // 6. 执行新增
            int addedCount = 0;
            for (File file : filesToAdd) {
                if (insertNewSong(file)) {
                    addedCount++;
                }
            }

            long end = System.currentTimeMillis();
            log.info("✅ 双向同步完成！耗时: {}ms，新增: {} 首，清理死链: {} 条",
                    (end - start), addedCount, idsToDelete.size());

        } catch (Exception e) {
            log.error("双向同步任务发生崩溃: ", e);
        }
    }

    private boolean insertNewSong(File file) {
        try {
            log.info("🎵 正在录入新歌曲: {}", file.getName());
            AudioFile f = AudioFileIO.read(file);
            Tag tag = f.getTag();
            AudioHeader header = f.getAudioHeader();

            Songs song = new Songs();
            // 快速指纹：文件名+大小的哈希，代替全量MD5，速度极快
            song.setFileMd5(java.util.Objects.hash(file.getName(), file.length()) + "");

            song.setUploaderId(1);
            song.setUploaderName("SystemScanner");

            String title = (tag != null) ? tag.getFirst(FieldKey.TITLE) : "";
            song.setSongTitle(StringUtils.hasText(title) ? title : file.getName());
            song.setArtist((tag != null) ? tag.getFirst(FieldKey.ARTIST) : "未知艺术家");
            song.setAlbum((tag != null) ? tag.getFirst(FieldKey.ALBUM) : "未知专辑");

            song.setDuration(header.getTrackLength());
            song.setBitrate((int) header.getBitRateAsNumber());
            song.setFileSize(String.valueOf(file.length()));
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