package com.kyf.mp.server.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.images.Artwork;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import com.kyf.mp.server.common.file.StoragePathResolver;
import com.kyf.mp.server.common.file.SongFileNames;
import com.kyf.mp.server.modules.song.entity.Songs;
import com.kyf.mp.server.modules.song.repository.SongsRepository;
import com.kyf.mp.server.modules.song.vo.SongVO;
import com.kyf.mp.server.modules.user.entity.Users;
import com.kyf.mp.server.modules.user.repository.UsersRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
    private final SongsRepository songsRepository;
    private final UsersRepository usersRepository;

    // 步骤一，扫描文件内容，把所有本地歌曲的信息都存进列表
    // 步骤二，扫描songs表，对比哪些歌有哪些歌无，返回列表
    // 步骤三，根据返回的列表进行数据维护进行数据维护

    // 想法一，先进行一次全新的数据写入，把之前的测试数据全删了，对应的静态资源也删了，
    // 然后重新扫一遍新编辑的歌，存入新版信息

    // 想法二，把这个任务用额外的线程跑起来，在运行时输入指令比如refresh会主动进行一次扫描更新songs表，
    // 不动已有记录避免运行时冲突

    // 获取文件路径和根据提取信息组装的song对象，此处未处理cover
    public List<Map<Path, Songs>> scanSongsFromDir(Long uploaderId, String uploaderName) throws IOException {
        List<Map<Path, Songs>> songWithPaths = new ArrayList<Map<Path, Songs>>();
        Path songDir = Path.of(songPath).toAbsolutePath();
        Integer sum = 0;
        Integer invalid = 0;
        Integer error = 0;
        try (Stream<Path> paths = Files.list(songDir)) {
            for (Path sp : paths.filter(Files::isRegularFile).toList()) {
                try {
                    AudioFile song = AudioFileIO.read(sp.toFile());
                    Tag tag = song.getTag();
                    if (tag == null) {
                        Songs songvo = new Songs();
                        songvo.setSongUrl(SongFileNames.versionedUrl(songUrlPrefix, sp.getFileName().toString()));
                        songWithPaths.add(Map.of(sp, songvo));
                        sum++;
                        invalid++;
                    } else {
                        Songs songvo = new Songs();
                        String songTitle = tag.getFirst(FieldKey.TITLE).isBlank() ? null : tag.getFirst(FieldKey.TITLE);
                        String artist = tag.getFirst(FieldKey.ARTIST).isBlank() ? null : tag.getFirst(FieldKey.ARTIST);
                        String album = tag.getFirst(FieldKey.ALBUM).isBlank() ? null : tag.getFirst(FieldKey.ALBUM);
                        Integer duration = song.getAudioHeader().getTrackLength();
                        Integer bitrate = Math.toIntExact(song.getAudioHeader().getBitRateAsNumber());
                        String format = song.getAudioHeader().getFormat();
                        LocalDateTime dateAdded = LocalDateTime.now();
                        String lyrics = null;
                        String tLyrics = null;
                        songvo.setUploaderId(1111111111111111111L);
                        songvo.setUploaderName("test");

                        songvo.setSongTitle(songTitle);
                        songvo.setArtist(artist);
                        songvo.setAlbum(album);
                        songvo.setDuration(duration);
                        songvo.setBitrate(bitrate);
                        songvo.setDateAdded(dateAdded);
                        songvo.setLyrics(lyrics);
                        songvo.setTLyrics(tLyrics);
                        songvo.setFileFormat(format);

                        String songName = sp.getFileName().toString();
                        String songUrl = SongFileNames.versionedUrl(songUrlPrefix, songName);

                        songvo.setSongUrl(songUrl);
                        songWithPaths.add(Map.of(sp, songvo));
                        sum++;
                    }
                } catch (Exception e) {
                    error++;
                    continue;
                }
            }
        }
        return songWithPaths;
    }

    // 扫描数据库获取路径和songs对象
    public List<Map<Path, Songs>> scanSongsFromSql() {
        List<Map<Path, Songs>> songWithPath = new ArrayList<Map<Path, Songs>>();
        List<Songs> songs = songsRepository.findAll();
        for (var song : songs) {
            String songName = SongFileNames.fileNameFromUrl(song.getSongUrl());
            Path path = Path.of(songPath).toAbsolutePath().resolve(songName);
            songWithPath.add(Map.of(path, song));
        }
        return songWithPath;
    }

    // 从零开始写入空表
    public void writeInSqlFromNull() {
        try {
            Integer suc = 0;
            Integer err = 0;
            Integer none = 0;
            Integer skip = 0;
            List<Map<Path, Songs>> songWithPaths = scanSongsFromDir(scannerUploaderId, songCoverPath);
            for (var songWithPath : songWithPaths) {
                Map.Entry<Path, Songs> el = songWithPath.entrySet().iterator().next();
                AudioFile audioFile = AudioFileIO.read(el.getKey().toFile());
                Songs song = el.getValue();
                Tag tag = audioFile.getTag();
                Artwork cover = tag.getFirstArtwork();
                if (cover == null) {
                    song.setSongCoverUrl(null);
                    none++;
                } else {
                    byte[] coverByte = cover.getBinaryData();
                    String postfix = cover.getMimeType().substring(6);
                    String songName = el.getKey().getFileName().toString();
                    String coverName = SongFileNames.coverName(songName, postfix);
                    String coverUrl = SongFileNames.publicUrl(songCoverUrlPrefix, coverName);
                    song.setSongCoverUrl(coverUrl);
                    Path path = Path.of(songCoverPath).toAbsolutePath().resolve(coverName);
                    try {
                        Files.write(path, coverByte);
                        songsRepository.insert(song);
                        suc++;
                    } catch (Exception e) {
                        err++;
                        if (Files.exists(path)) {
                            Files.delete(path);
                        }
                        System.out.println(e);
                    }
                }
            }
            System.out.println("失败:" + err + "  为空:" + none + "  成功:" + suc + " 跳过:" + skip);
        } catch (Exception e) {

        }
    }

    // 重命名音乐文件
    public void renameSongFile() {
        Path songpath = Path.of(songPath).toAbsolutePath();
        Integer suc = 0;
        Integer err = 0;
        try (Stream<Path> paths = Files.list(songpath).filter(Files::isRegularFile)) {
            for (Path path : paths.toList()) {
                try {
                    Integer index = path.toString().lastIndexOf(".");
                    String postfix = path.toString().substring(index + 1);
                    AudioFile audio = AudioFileIO.read(path.toFile());
                    Tag tag = audio.getTag();
                    String songTitle = tag.getFirst(FieldKey.TITLE).equals("") ? "未命名" : tag.getFirst(FieldKey.TITLE);
                    String completeName = SongFileNames.audioName(songTitle, postfix);
                    Path finalPath = Path.of(songPath).toAbsolutePath().resolve(completeName);
                    Files.move(path, finalPath);
                    suc++;
                } catch (Exception e) {
                    err++;
                }
            }
        } catch (Exception e) {

        }
        System.out.println("sum:" + err + suc + "\n suc:" + suc);
    }

    @Async
    @EventListener(ApplicationReadyEvent.class)
    public void MaintainFilesAndSchema() {
        try {
            // log.info("启动写入任务");
            // writeInSqlFromNull();
        } catch (Exception e) {

        }
    }

    // @Async
    // @EventListener(ApplicationReadyEvent.class)
    // public void runTask() {
    // if (!isScannerConfigured()) {
    // return;
    // }
    // Users scannerUploader = usersRepository.getById(scannerUploaderId);
    // if (scannerUploader == null) {
    // log.error("Song scanner uploader does not exist, userId={}",
    // scannerUploaderId);
    // return;
    // }
    // log.info("Starting song scan...");
    // long start = System.currentTimeMillis();

    // try {
    // File[] files = scanSongFiles();
    // if (files == null) {
    // return;
    // }

    // ScanResult result = compareWithDatabase(files);
    // removeMissingSongs(result.idsToDelete());
    // int addedCount = addNewSongs(result.filesToAdd(), scannerUploader);
    // logCompletion(start, addedCount, result.idsToDelete().size());
    // } catch (Exception e) {
    // log.error("Song scan task failed: ", e);
    // }
    // }

    // private boolean isScannerConfigured() {
    // if (scannerUploaderId == null || scannerUploaderId <= 0) {
    // log.error("song.scanner.uploader-id must be a valid user ID");
    // return false;
    // }
    // return true;
    // }

    // private File[] scanSongFiles() {
    // File[] files = StoragePathResolver.resolveDirectory(songPath).toFile()
    // .listFiles((d, name) -> isSupportedSong(name));
    // if (files == null) {
    // log.warn("Song directory does not exist or is not readable, path={}",
    // songPath);
    // }
    // return files;
    // }

    // private boolean isSupportedSong(String name) {
    // String extension = name.toLowerCase();
    // return extension.endsWith(".mp3") || extension.endsWith(".flac")
    // || extension.endsWith(".wav") || extension.endsWith(".m4a");
    // }

    // private ScanResult compareWithDatabase(File[] files) {
    // Set<String> localUrls = buildLocalUrls(files);
    // List<Songs> dbSongs = songsRepository.findScannerSongs(scannerUploaderId);
    // Set<String> dbUrls =
    // dbSongs.stream().map(Songs::getSongUrl).collect(Collectors.toSet());
    // List<Long> idsToDelete = dbSongs.stream()
    // .filter(song -> !localUrls.contains(song.getSongUrl()))
    // .map(Songs::getSongId)
    // .collect(Collectors.toList());
    // return new ScanResult(idsToDelete, findNewFiles(files, dbUrls));
    // }

    // private Set<String> buildLocalUrls(File[] files) {
    // Set<String> localUrls = new HashSet<>();
    // for (File file : files) {
    // localUrls.add(songUrlPrefix + file.getName());
    // }
    // return localUrls;
    // }

    // private List<File> findNewFiles(File[] files, Set<String> dbUrls) {
    // List<File> filesToAdd = new ArrayList<>();
    // for (File file : files) {
    // if (!dbUrls.contains(songUrlPrefix + file.getName())) {
    // filesToAdd.add(file);
    // }
    // }
    // return filesToAdd;
    // }

    // private void removeMissingSongs(List<Long> idsToDelete) {
    // if (idsToDelete.isEmpty()) {
    // return;
    // }
    // if (removeMissingSongs) {
    // log.info("Removing {} missing song records", idsToDelete.size());
    // songsRepository.deleteBatchIds(idsToDelete);
    // return;
    // }
    // log.warn("Found {} missing song records; removal is disabled",
    // idsToDelete.size());
    // }

    // private int addNewSongs(List<File> filesToAdd, Users scannerUploader) {
    // int addedCount = 0;
    // for (File file : filesToAdd) {
    // if (insertNewSong(file, scannerUploader)) {
    // addedCount++;
    // }
    // }
    // return addedCount;
    // }

    // private void logCompletion(long start, int addedCount, int removedCount) {
    // log.info("Song scan completed: {}ms, added: {}, removed: {}",
    // (System.currentTimeMillis() - start), addedCount,
    // removeMissingSongs ? removedCount : 0);
    // }

    // private record ScanResult(List<Long> idsToDelete, List<File> filesToAdd) {
    // }

    // private boolean insertNewSong(File file, Users scannerUploader) {
    // try {
    // log.info("🎵 正在录入新歌 {}", file.getName());
    // AudioFile f = AudioFileIO.read(file);
    // Tag tag = f.getTag();
    // AudioHeader header = f.getAudioHeader();

    // Songs song = new Songs();
    // // 快速指纹：代替全量MD5，速度极快
    // song.setFileMd5(calculateMd5(file));

    // song.setUploaderId(scannerUploaderId);
    // song.setUploaderName(scannerUploader.getUserName());

    // String title = (tag != null) ? tag.getFirst(FieldKey.TITLE) : "";
    // song.setSongTitle(StringUtils.hasText(title) ? title : file.getName());
    // song.setArtist((tag != null) ? tag.getFirst(FieldKey.ARTIST) : "未知艺术家");
    // song.setAlbum((tag != null) ? tag.getFirst(FieldKey.ALBUM) : "未知专辑");

    // song.setDuration(header.getTrackLength());
    // song.setBitrate((int) header.getBitRateAsNumber());
    // song.setFileSize(file.length());
    // song.setFileFormat(StringUtils.getFilenameExtension(file.getName()));
    // song.setSongUrl(songUrlPrefix + file.getName());
    // song.setDateAdded(LocalDateTime.now());

    // // 封面处理
    // song.setSongCoverUrl(extractArtwork(tag));

    // songsRepository.insert(song);
    // return true;
    // } catch (Exception e) {
    // log.error("解析失败: {}", file.getName(), e);
    // return false;
    // }
    // }

    // private String calculateMd5(File file) throws IOException {
    // try (InputStream inputStream = new FileInputStream(file)) {
    // return DigestUtils.md5DigestAsHex(inputStream);
    // }
    // }

    // private String extractArtwork(Tag tag) {
    // if (tag == null)
    // return null;
    // try {
    // Artwork artwork = tag.getFirstArtwork();
    // if (artwork != null) {
    // byte[] data = artwork.getBinaryData();
    // String coverName = "auto-" + UUID.randomUUID().toString().substring(0, 8) +
    // ".jpg";
    // File coverFile = StoragePathResolver.resolveFile(songCoverPath, coverName);
    // if (!coverFile.getParentFile().exists()) {
    // boolean created = coverFile.getParentFile().mkdirs();
    // log.info("创建封面目录: {}, 结果: {}", coverFile.getParentFile().getAbsolutePath(),
    // created);
    // }
    // try (FileOutputStream fos = new FileOutputStream(coverFile)) {
    // fos.write(data);
    // }
    // return songCoverUrlPrefix + coverName;
    // }
    // } catch (Exception ignored) {
    // }
    // return null;
    // }
}
