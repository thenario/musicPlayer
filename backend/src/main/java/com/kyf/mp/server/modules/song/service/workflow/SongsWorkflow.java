package com.kyf.mp.server.modules.song.service.workflow;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.images.ArtworkFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kyf.mp.server.common.BusinessException;
import com.kyf.mp.server.common.file.FileUpdateBatch;
import com.kyf.mp.server.common.file.SongFileNames;
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

    public GetSongsVO getSongsPage(Integer page, String keyword, Integer size) {
        int current = (page == null || page < 1) ? 1 : page;
        int pageLimit = size == null ? songPageSize : size;

        Page<Songs> pageConfig = new Page<>(current, pageLimit);
        pageConfig.setOptimizeCountSql(false);

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
            Files.createDirectories(savedAudioFile.getParentFile().toPath());
            Files.createDirectories(savedCoverFile.getParentFile().toPath());
            audioFile.transferTo(savedAudioFile);
            coverFile.transferTo(savedCoverFile);
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
            String title, String artist, String album) {
        String audioExt = UploadFileValidator.validateAudio(audioFile);
        String coverExt = UploadFileValidator.validateImage(coverFile);
        String finalTitle = StringUtils.hasText(title) ? title : "未命名";
        String finalArtist = StringUtils.hasText(artist) ? artist : "Unknown Artist";
        String finalAlbum = StringUtils.hasText(album) ? album : "Unknown Album";
        String audioName = SongFileNames.audioName(finalTitle, audioExt);
        String coverName = SongFileNames.coverName(audioName, coverExt);
        File audioTarget = StoragePathResolver.resolveFile(songPath, audioName);
        File coverTarget = StoragePathResolver.resolveFile(songCoverPath, coverName);
        return new UploadContext(audioTarget, coverTarget, audioName, coverName,
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
        tag.deleteArtworkField();
        tag.setField(ArtworkFactory.createArtworkFromFile(context.coverFile()));
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
        song.setFileFormat(header.getFormat());
        song.setLyrics(lyrics);
        song.setSongUrl(SongFileNames.publicUrl(songUrl, context.audioName()));
        song.setSongCoverUrl(coverFile != null ? versionedCoverUrl(context.coverName()) : null);
        song.setDateAdded(LocalDateTime.now(ZoneId.systemDefault()));
        return song;
    }

    private record UploadContext(File audioFile, File coverFile, String audioName, String coverName,
            String title, String artist, String album) {
    }

    private String versionedCoverUrl(String fileName) {
        return versionedUrl(songCoverUrl, fileName);
    }

    private String versionedUrl(String prefix, String fileName) {
        return SongFileNames.versionedUrl(prefix, fileName);
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
        Songs updateEntity = createSongUpdate(dto, oldSong);
        boolean newCover = dto.getSong_cover() != null && !dto.getSong_cover().isEmpty();
        if (!hasChanges(updateEntity) && !newCover) {
            return;
        }
        FileUpdateBatch files = new FileUpdateBatch();
        try {
            if (audioMetadataChanged(updateEntity) || newCover) {
                prepareEditedFiles(dto, oldSong, updateEntity, files, newCover);
            }
            files.publish();
            if (!songsRepository.updateById(updateEntity)) {
                throw new BusinessException(409, "歌曲信息更新失败，请刷新后重试");
            }
            files.completeAfterTransaction();
        } catch (BusinessException exception) {
            files.rollback();
            throw exception;
        } catch (Exception exception) {
            files.rollback();
            log.error("更新歌曲文件及元信息失败，歌曲ID：{}", songId, exception);
            throw new BusinessException(500, "歌曲文件更新失败");
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

    private Songs createSongUpdate(EditSongDTO dto, Songs oldSong) {
        Songs updateEntity = new Songs();
        updateEntity.setSongId(oldSong.getSongId());
        if (StringUtils.hasText(dto.getSong_name()) && !Objects.equals(dto.getSong_name(), oldSong.getSongTitle())) {
            updateEntity.setSongTitle(dto.getSong_name());
        }
        if (dto.getArtist() != null && !Objects.equals(dto.getArtist(), oldSong.getArtist())) {
            updateEntity.setArtist(dto.getArtist());
        }
        if (dto.getAlbum() != null && !Objects.equals(dto.getAlbum(), oldSong.getAlbum())) {
            updateEntity.setAlbum(dto.getAlbum());
        }
        if (dto.getLyrics() != null && !Objects.equals(dto.getLyrics(), oldSong.getLyrics())) {
            updateEntity.setLyrics(dto.getLyrics());
        }
        if (dto.getT_lyrics() != null && !Objects.equals(dto.getT_lyrics(), oldSong.getTLyrics())) {
            updateEntity.setTLyrics(dto.getT_lyrics());
        }
        return updateEntity;
    }

    private void prepareEditedFiles(EditSongDTO dto, Songs oldSong, Songs updateEntity,
            FileUpdateBatch files, boolean newCover) throws Exception {
        if (!StringUtils.hasText(oldSong.getSongUrl())) {
            throw new BusinessException(404, "歌曲音频文件不存在");
        }
        String oldAudioName = SongFileNames.fileNameFromUrl(oldSong.getSongUrl());
        Path originalAudio = StoragePathResolver.resolveFile(songPath, oldAudioName).toPath();
        if (!Files.isRegularFile(originalAudio)) {
            throw new BusinessException(404, "歌曲音频文件不存在");
        }
        String audioName = updateEntity.getSongTitle() == null ? oldAudioName
                : SongFileNames.renamedAudioName(oldAudioName, updateEntity.getSongTitle());
        Path stagedCover = prepareEditedCover(dto.getSong_cover(), oldSong, updateEntity, files,
                newCover, !audioName.equals(oldAudioName), audioName);
        Path targetAudio = StoragePathResolver.resolveFile(songPath, audioName).toPath();
        Path stagedAudio = files.stage(originalAudio, targetAudio);
        Files.copy(originalAudio, stagedAudio);
        AudioFile audio = AudioFileIO.read(stagedAudio.toFile());
        Tag tag = audio.getTagOrCreateAndSetDefault();
        updateTag(tag, FieldKey.TITLE, updateEntity.getSongTitle());
        updateTag(tag, FieldKey.ARTIST, updateEntity.getArtist());
        updateTag(tag, FieldKey.ALBUM, updateEntity.getAlbum());
        updateTag(tag, FieldKey.LYRICS, updateEntity.getLyrics());
        if (newCover) {
            tag.deleteArtworkField();
            tag.setField(ArtworkFactory.createArtworkFromFile(stagedCover.toFile()));
        }
        audio.commit();
        updateEntity.setSongUrl(versionedUrl(songUrl, audioName));
        updateEntity.setFileMd5(calculateMD5(stagedAudio.toFile()));
        updateEntity.setFileSize(Files.size(stagedAudio));
        updateEntity.setFileFormat(audio.getAudioHeader().getFormat());
        updateEntity.setDuration(audio.getAudioHeader().getTrackLength());
        updateEntity.setBitrate(Math.toIntExact(audio.getAudioHeader().getBitRateAsNumber()));
    }

    private Path prepareEditedCover(MultipartFile cover, Songs oldSong, Songs updateEntity,
            FileUpdateBatch files, boolean newCover, boolean renamed, String audioName) throws IOException {
        Path original = null;
        String oldName = null;
        if (StringUtils.hasText(oldSong.getSongCoverUrl())) {
            oldName = SongFileNames.fileNameFromUrl(oldSong.getSongCoverUrl());
            original = StoragePathResolver.resolveFile(songCoverPath, oldName).toPath();
        }
        if (!newCover && (!renamed || original == null)) {
            return null;
        }
        if (!newCover && !Files.isRegularFile(original)) {
            throw new BusinessException(404, "歌曲封面文件不存在，请重新上传封面");
        }
        String extension = newCover ? UploadFileValidator.validateImage(cover)
                : StringUtils.getFilenameExtension(oldName);
        String coverName = SongFileNames.coverName(audioName, extension);
        Path target = StoragePathResolver.resolveFile(songCoverPath, coverName).toPath();
        Path staged = files.stage(original, target);
        if (newCover) {
            cover.transferTo(staged.toFile());
        } else {
            Files.copy(original, staged);
        }
        updateEntity.setSongCoverUrl(versionedCoverUrl(coverName));
        return staged;
    }

    private void updateTag(Tag tag, FieldKey key, String value) throws Exception {
        if (value != null) {
            if (value.isBlank()) {
                tag.deleteField(key);
            } else {
                tag.setField(key, value);
            }
        }
    }

    private boolean audioMetadataChanged(Songs updateEntity) {
        return updateEntity.getSongTitle() != null || updateEntity.getArtist() != null
                || updateEntity.getAlbum() != null || updateEntity.getLyrics() != null;
    }

    private boolean hasChanges(Songs updateEntity) {
        return audioMetadataChanged(updateEntity)
                || updateEntity.getTLyrics() != null || updateEntity.getSongCoverUrl() != null;
    }
}
