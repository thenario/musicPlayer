package com.kyf.mp.server.modules.song.service.workflow;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kyf.mp.server.common.BusinessException;
import com.kyf.mp.server.common.file.StoragePathResolver;
import com.kyf.mp.server.common.file.UploadFileValidator;
import com.kyf.mp.server.modules.song.dto.EditSongDTO;
import com.kyf.mp.server.modules.song.entity.Songs;
import com.kyf.mp.server.modules.song.repository.SongsRepository;
import com.kyf.mp.server.modules.song.vo.GetSongsVO;
import com.kyf.mp.server.modules.song.vo.LyricsVO;
import com.kyf.mp.server.modules.user.entity.Users;
import com.kyf.mp.server.modules.user.repository.UsersRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** 业务流程：权限校验、事务编排、文件处理与结果组装；数据库操作委托给 Repository。 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SongsWorkflow {

    private final UsersRepository userRepository;
    private final SongsRepository songsRepository;

    @Value("${file.upload.song-path}")
    private String songPath;

    @Value("${file.upload.song-cover-path}")
    private String songCoverPath;

    @Value("${file.static.song-url}")
    private String songUrl;

    @Value("${file.static.song-cover-url}")
    private String songCoverUrl;

    @Value("${song.page-size:15}")
    private int songPageSize;

    public GetSongsVO getSongsPage(Integer page, String keyword) {
        int current = (page == null || page < 1) ? 1 : page;
        int pageLimit = songPageSize;

        Page<Songs> pageConfig = new Page<>(current, pageLimit);

        var result = songsRepository.findPage(pageConfig, keyword);

        GetSongsVO vo = new GetSongsVO();
        vo.setSongs(result.getRecords());
        vo.setPagination(new GetSongsVO.PaginationVO(
                (int) result.getTotal(),
                (int) result.getPages(),
                (int) result.getCurrent(),
                pageLimit));

        return vo;
    }

    public LyricsVO getLyrics(Long songId) {
        Songs song = songsRepository.findLyrics(songId);

        if (song == null) {
            throw new BusinessException(404, "未找到该歌曲或歌词已下架");
        }

        LyricsVO vo = new LyricsVO();
        vo.setLyrics(StringUtils.hasText(song.getLyrics()) ? song.getLyrics() : "");
        vo.setTLyrics(StringUtils.hasText(song.getTLyrics()) ? song.getTLyrics() : "");

        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public void uploadSong(MultipartFile audioFile, MultipartFile coverFile, Long userId,
            String title, String artist, String album, String lyrics) {
        File savedAudioFile = null;
        File savedCoverFile = null;
        try {
            UploadContext context = prepareUpload(audioFile, coverFile, title, artist, album);
            savedAudioFile = context.audioFile();
            savedCoverFile = context.coverFile();
            AudioHeader header = writeAudioMetadata(context, lyrics);
            Songs song = buildUploadedSong(context, header, coverFile, lyrics, userId);
            songsRepository.insert(song);
        } catch (CannotReadException exception) {
            cleanupFiles(savedAudioFile, savedCoverFile);
            throw new BusinessException(400, "音频文件内容无效");
        } catch (BusinessException exception) {
            cleanupFiles(savedAudioFile, savedCoverFile);
            throw exception;
        } catch (Exception exception) {
            cleanupFiles(savedAudioFile, savedCoverFile);
            log.error("上传歌曲失败", exception);
            throw new BusinessException(500, "上传失败");
        }
    }

    private UploadContext prepareUpload(MultipartFile audioFile, MultipartFile coverFile,
            String title, String artist, String album) throws IOException {
        String audioExt = UploadFileValidator.validateAudio(audioFile);
        String coverExt = UploadFileValidator.validateImage(coverFile);
        String finalTitle = StringUtils.hasText(title) ? title : "Unknown Title";
        String finalArtist = StringUtils.hasText(artist) ? artist : "Unknown Artist";
        String finalAlbum = StringUtils.hasText(album) ? album : "Unknown Album";
        String safeName = (finalArtist + " - " + finalTitle).replaceAll("[\\\\/:*?\"<>|]", "_");
        String audioName = safeName + "_" + UUID.randomUUID().toString().substring(0, 8) + "." + audioExt;
        String coverName = "cover-" + UUID.randomUUID().toString().substring(0, 8) + "." + coverExt;
        File audioTarget = StoragePathResolver.resolveFile(songPath, audioName);
        File coverTarget = StoragePathResolver.resolveFile(songCoverPath, coverName);
        Files.createDirectories(audioTarget.getParentFile().toPath());
        Files.createDirectories(coverTarget.getParentFile().toPath());
        audioFile.transferTo(audioTarget);
        if (coverFile != null && !coverFile.isEmpty()) {
            coverFile.transferTo(coverTarget);
        }
        return new UploadContext(audioTarget, coverTarget, audioName, coverName, audioExt,
                finalTitle, finalArtist, finalAlbum);
    }

    private AudioHeader writeAudioMetadata(UploadContext context, String lyrics) throws Exception {
        AudioFile audio = AudioFileIO.read(context.audioFile());
        Tag tag = audio.getTagOrCreateAndSetDefault();
        tag.setField(FieldKey.TITLE, context.title());
        tag.setField(FieldKey.ARTIST, context.artist());
        tag.setField(FieldKey.ALBUM, context.album());
        if (StringUtils.hasText(lyrics)) {
            tag.setField(FieldKey.LYRICS, lyrics);
        }
        audio.commit();
        return audio.getAudioHeader();
    }

    private Songs buildUploadedSong(UploadContext context, AudioHeader header,
            MultipartFile coverFile, String lyrics, Long userId) throws IOException {
        Users user = userRepository.getById(userId);
        if (user == null) {
            throw new BusinessException(401, "上传失败：上传者身份无效");
        }
        Songs song = new Songs();
        song.setFileMd5(calculateMD5(context.audioFile()));
        song.setUploaderId(userId);
        song.setUploaderName(user.getUserName());
        song.setSongTitle(context.title());
        song.setArtist(context.artist());
        song.setAlbum(context.album());
        song.setFileSize(context.audioFile().length());
        song.setDuration(header.getTrackLength());
        song.setBitrate((int) header.getBitRateAsNumber());
        song.setFileFormat(context.audioExtension());
        song.setLyrics(lyrics);
        song.setSongUrl(songUrl + context.audioName());
        song.setSongCoverUrl(coverFile != null ? songCoverUrl + context.coverName() : null);
        song.setDateAdded(LocalDateTime.now(ZoneId.systemDefault()));
        return song;
    }

    private record UploadContext(File audioFile, File coverFile, String audioName, String coverName,
            String audioExtension, String title, String artist, String album) {
    }

    private String calculateMD5(File file) throws IOException {
        try (java.io.FileInputStream fis = new java.io.FileInputStream(file)) {
            return org.springframework.util.DigestUtils.md5DigestAsHex(fis);
        }
    }

    private void cleanupFiles(File audio, File cover) {
        cleanupFile(audio);
        cleanupFile(cover);
    }

    private void cleanupFile(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        try {
            Files.deleteIfExists(file.toPath());
        } catch (IOException exception) {
            log.warn("Unable to delete file: {}", file.getAbsolutePath(), exception);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void editUploadSong(EditSongDTO dto, Long userId, Long songId) {
        Songs oldSong = getEditableSong(songId, userId);
        Songs updateEntity = createSongUpdate(dto, songId);
        boolean coverChanged = replaceCover(dto.getSong_cover(), oldSong, userId, updateEntity);
        if (hasChanges(updateEntity) || coverChanged) {
            songsRepository.updateById(updateEntity);
        }
    }

    private Songs getEditableSong(Long songId, Long userId) {
        Songs song = songsRepository.getById(songId);
        if (song == null) {
            throw new BusinessException(404, "歌曲不存在");
        }
        if (!song.getUploaderId().equals(userId)) {
            throw new BusinessException(403, "你没有权限修改这首歌");
        }
        return song;
    }

    private Songs createSongUpdate(EditSongDTO dto, Long songId) {
        Songs updateEntity = new Songs();
        updateEntity.setSongId(songId);
        if (StringUtils.hasText(dto.getSong_name())) {
            updateEntity.setSongTitle(dto.getSong_name());
        }
        if (dto.getLyrics() != null) {
            updateEntity.setLyrics(dto.getLyrics());
        }
        if (dto.getT_lyrics() != null) {
            updateEntity.setTLyrics(dto.getT_lyrics());
        }
        return updateEntity;
    }

    private boolean replaceCover(MultipartFile newCover, Songs oldSong, Long userId,
            Songs updateEntity) {
        if (newCover == null || newCover.isEmpty()) {
            return false;
        }
        String extension = UploadFileValidator.validateImage(newCover);
        String fileName = "cover-" + UUID.randomUUID().toString().substring(0, 8) + "." + extension;
        File destFile = StoragePathResolver.resolveFile(songCoverPath, fileName);
        try {
            Files.createDirectories(destFile.getParentFile().toPath());
            newCover.transferTo(destFile);
            cleanupOldCover(oldSong.getSongCoverUrl(), userId);
            updateEntity.setSongCoverUrl(songCoverUrl + fileName);
            return true;
        } catch (IOException exception) {
            cleanupFile(destFile);
            log.error("封面物理保存失败", exception);
            throw new BusinessException(500, "文件保存失败");
        }
    }

    private void cleanupOldCover(String oldUrl, Long userId) {
        if (!StringUtils.hasText(oldUrl)) {
            return;
        }
        String fileName = oldUrl.substring(oldUrl.lastIndexOf("/") + 1);
        cleanupFile(StoragePathResolver.resolveFile(songCoverPath, fileName));
        log.info("用户ID: {} 修改歌曲，已清理旧封面: {}", userId, fileName);
    }

    private boolean hasChanges(Songs updateEntity) {
        return updateEntity.getSongTitle() != null || updateEntity.getLyrics() != null
                || updateEntity.getTLyrics() != null || updateEntity.getSongCoverUrl() != null;
    }
}
