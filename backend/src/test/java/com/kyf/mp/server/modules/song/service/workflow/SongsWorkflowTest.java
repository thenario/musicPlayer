package com.kyf.mp.server.modules.song.service.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.util.UriUtils;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kyf.mp.server.common.BusinessException;
import com.kyf.mp.server.modules.song.dto.EditSongDTO;
import com.kyf.mp.server.modules.song.entity.Songs;
import com.kyf.mp.server.modules.song.mapper.SongsMapper;
import com.kyf.mp.server.modules.song.repository.impl.SongsRepositoryImpl;
import com.kyf.mp.server.modules.song.vo.GetSongsVO;
import com.kyf.mp.server.modules.song.vo.LyricsVO;
import com.kyf.mp.server.modules.user.mapper.UsersMapper;
import com.kyf.mp.server.modules.user.entity.Users;
import com.kyf.mp.server.modules.user.repository.impl.UsersRepositoryImpl;

@ExtendWith(MockitoExtension.class)
class SongsWorkflowTest {

    @Mock
    private SongsMapper songsMapper;
    @Mock
    private UsersMapper usersMapper;

    private SongsWorkflow songsWorkflow;

    @TempDir
    private Path storage;

    @BeforeEach
    void setUp() {
        if (TableInfoHelper.getTableInfo(Songs.class) == null) {
            TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new Configuration(), ""), Songs.class);
        }
        songsWorkflow = new SongsWorkflow(new UsersRepositoryImpl(usersMapper),
                new SongsRepositoryImpl(songsMapper));
        ReflectionTestUtils.setField(songsWorkflow, "songPageSize", 15);
        ReflectionTestUtils.setField(songsWorkflow, "songPath", storage.resolve("songs").toString());
        ReflectionTestUtils.setField(songsWorkflow, "songCoverPath", storage.resolve("covers").toString());
        ReflectionTestUtils.setField(songsWorkflow, "songUrl", "/static/songs/");
        ReflectionTestUtils.setField(songsWorkflow, "songCoverUrl", "/static/song_covers/");
    }

    @Test
    @DisplayName("歌曲存在且歌词为空时返回空字符串")
    void returnsEmptyLyricsForBlankLyrics() {
        Songs song = new Songs();
        song.setLyrics("  ");
        song.setTLyrics(null);
        when(songsMapper.selectOne(any(Wrapper.class))).thenReturn(song);

        LyricsVO result = songsWorkflow.getLyrics(1L);

        assertThat(result.getLyrics()).isEmpty();
        assertThat(result.getTLyrics()).isEmpty();
    }

    @Test
    @DisplayName("歌曲不存在时查询歌词抛出404")
    void rejectsMissingSongLyrics() {
        when(songsMapper.selectOne(any(Wrapper.class))).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> songsWorkflow.getLyrics(1L));

        assertThat(exception.getCode()).isEqualTo(404);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(ints = {10, 50})
    @DisplayName("歌曲分页使用传入条数，未传时使用配置值")
    void returnsSongsPage(Integer size) {
        int expectedSize = size == null ? 15 : size;
        Songs song = new Songs();
        song.setSongId(8L);
        Page<Songs> page = new Page<>(2, expectedSize);
        page.setTotal(80);
        page.setRecords(List.of(song));
        when(songsMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(page);

        GetSongsVO result = songsWorkflow.getSongsPage(2, "hello", size);

        verify(songsMapper).selectPage(argThat((Page<Songs> query) ->
                query.getCurrent() == 2 && query.getSize() == expectedSize), any(Wrapper.class));
        assertThat(result.getSongs()).containsExactly(song);
        assertThat(result.getPagination().getTotalItems()).isEqualTo(80);
        assertThat(result.getPagination().getCurrentPage()).isEqualTo(2);
        assertThat(result.getPagination().getPageLimit()).isEqualTo(expectedSize);
    }

    @Test
    @DisplayName("编辑歌曲时没有修改字段则不更新数据库")
    void doesNotUpdateSongWhenNothingChanged() {
        Songs oldSong = new Songs();
        oldSong.setSongId(8L);
        oldSong.setUploaderId(2L);
        when(songsMapper.selectById(8L)).thenReturn(oldSong);

        songsWorkflow.editUploadSong(new EditSongDTO(), 2L, 8L);

        verify(songsMapper, never()).updateById(any(Songs.class));
    }

    @Test
    @DisplayName("编辑歌曲时上传者不匹配抛出403")
    void rejectsEditByAnotherUser() {
        Songs oldSong = new Songs();
        oldSong.setSongId(8L);
        oldSong.setUploaderId(2L);
        when(songsMapper.selectById(8L)).thenReturn(oldSong);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> songsWorkflow.editUploadSong(new EditSongDTO(), 3L, 8L));

        assertThat(exception.getCode()).isEqualTo(403);
    }

    @Test
    @DisplayName("上传音频和封面共享完整UUID命名，保留歌曲标题并规范JPEG后缀")
    void uploadsMatchingAudioAndCoverNames() throws Exception {
        stubUploader();
        MockMultipartFile cover = image("jpg");
        String title = "24⧸7 / live?";

        songsWorkflow.uploadSong(wavAudio(), cover, 2L, title, "歌手", "专辑", "歌词");

        ArgumentCaptor<Songs> inserted = ArgumentCaptor.forClass(Songs.class);
        verify(songsMapper).insert(inserted.capture());
        Songs song = inserted.getValue();
        String audioName = UriUtils.decode(song.getSongUrl().substring("/static/songs/".length()), StandardCharsets.UTF_8);
        assertThat(audioName).matches("audio-24⧸7 _ live_-[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}\\.wav");
        String coverName = audioName.substring(0, audioName.length() - 4) + "-cover-.jpeg";
        assertVersionedCoverUrl(song.getSongCoverUrl(), "/static/song_covers/" + coverName);
        assertThat(Files.readAllBytes(storage.resolve("covers").resolve(coverName)))
                .isEqualTo(cover.getBytes());
        AudioFile savedAudio = AudioFileIO.read(storage.resolve("songs").resolve(audioName).toFile());
        assertThat(savedAudio.getTag().getFirst(FieldKey.TITLE)).isEqualTo(title);
        assertThat(savedAudio.getTag().getFirstArtwork().getBinaryData()).isEqualTo(cover.getBytes());
        assertThat(song.getSongTitle()).isEqualTo(title);
        assertThat(song.getFileFormat()).isEqualTo(savedAudio.getAudioHeader().getFormat());
        assertThat(song.getDuration()).isEqualTo(1);
    }

    @Test
    @DisplayName("未提供标题时上传文件和数据库都使用未命名")
    void uploadsUnnamedSongWithDefaultTitle() throws Exception {
        stubUploader();

        songsWorkflow.uploadSong(wavAudio(), image("png"), 2L, " ", null, null, null);

        ArgumentCaptor<Songs> inserted = ArgumentCaptor.forClass(Songs.class);
        verify(songsMapper).insert(inserted.capture());
        assertThat(inserted.getValue().getSongTitle()).isEqualTo("未命名");
        assertThat(UriUtils.decode(inserted.getValue().getSongUrl(), StandardCharsets.UTF_8))
                .startsWith("/static/songs/audio-未命名-");
        String audioName = UriUtils.decode(inserted.getValue().getSongUrl().substring("/static/songs/".length()),
                StandardCharsets.UTF_8);
        String coverName = audioName.substring(0, audioName.length() - 4) + "-cover-.png";
        assertVersionedCoverUrl(inserted.getValue().getSongCoverUrl(), "/static/song_covers/" + coverName);
    }

    @Test
    @DisplayName("上传封面写入失败时清理已经保存的音频和部分封面")
    void cleansUploadWhenCoverTransferFails() throws Exception {
        MockMultipartFile cover = failingImage();

        assertThrows(BusinessException.class,
                () -> songsWorkflow.uploadSong(wavAudio(), cover, 2L, "歌曲", null, null, null));

        assertStorageEmpty();
        verify(songsMapper, never()).insert(any(Songs.class));
    }

    @Test
    @DisplayName("数据库写入失败时清理上传的配对文件")
    void cleansUploadWhenInsertFails() throws Exception {
        stubUploader();
        doThrow(new IllegalStateException("数据库写入失败")).when(songsMapper).insert(any(Songs.class));

        assertThrows(BusinessException.class,
                () -> songsWorkflow.uploadSong(wavAudio(), image("png"), 2L, "歌曲", null, null, null));

        assertStorageEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"png", "jpeg"})
    @DisplayName("更换封面沿用音频主体，清理旧格式并更新缓存版本")
    void replacesCoverUsingExistingAudioName(String oldExtension) throws Exception {
        String stem = "audio-24⧸7-68ec03f0-5e8f-4833-9030-1f0a3a6a39ed";
        Path covers = Files.createDirectories(storage.resolve("covers"));
        Path oldCover = covers.resolve(stem + "-cover-." + oldExtension);
        Files.write(oldCover, new byte[] {1, 2, 3});
        Songs song = editableSong(stem, oldExtension);
        when(songsMapper.selectById(8L)).thenReturn(song);
        when(songsMapper.updateById(any(Songs.class))).thenReturn(1);
        MockMultipartFile cover = image("png");
        EditSongDTO dto = new EditSongDTO();
        dto.setSong_cover(cover);

        songsWorkflow.editUploadSong(dto, 2L, 8L);

        ArgumentCaptor<Songs> updated = ArgumentCaptor.forClass(Songs.class);
        verify(songsMapper).updateById(updated.capture());
        assertVersionedCoverUrl(updated.getValue().getSongCoverUrl(),
                "/static/song_covers/" + stem + "-cover-.png");
        assertThat(Files.readAllBytes(covers.resolve(stem + "-cover-.png"))).isEqualTo(cover.getBytes());
        AudioFile audio = AudioFileIO.read(storage.resolve("songs").resolve(stem + ".wav").toFile());
        assertThat(audio.getTag().getFirstArtwork().getBinaryData()).isEqualTo(cover.getBytes());
        try (Stream<Path> paths = Files.list(covers)) {
            assertThat(paths.toList()).containsExactly(covers.resolve(stem + "-cover-.png"));
        }
    }

    @Test
    @DisplayName("更换封面写入失败时保留原封面并删除临时文件")
    void preservesCoverWhenReplacementTransferFails() throws Exception {
        String stem = "audio-歌曲-68ec03f0-5e8f-4833-9030-1f0a3a6a39ed";
        Path covers = Files.createDirectories(storage.resolve("covers"));
        Path oldCover = covers.resolve(stem + "-cover-.png");
        byte[] oldBytes = {1, 2, 3};
        Files.write(oldCover, oldBytes);
        when(songsMapper.selectById(8L)).thenReturn(editableSong(stem, "png"));
        EditSongDTO dto = new EditSongDTO();
        dto.setSong_cover(failingImage());

        assertThrows(BusinessException.class, () -> songsWorkflow.editUploadSong(dto, 2L, 8L));

        assertThat(Files.readAllBytes(oldCover)).isEqualTo(oldBytes);
        try (Stream<Path> paths = Files.list(covers)) {
            assertThat(paths.toList()).containsExactly(oldCover);
        }
        verify(songsMapper, never()).updateById(any(Songs.class));
    }

    private void stubUploader() {
        Users user = new Users();
        user.setUserName("上传者");
        when(usersMapper.selectById(2L)).thenReturn(user);
    }

    @Test
    @DisplayName("编辑元信息同步音频标签，改名保留UUID并同步封面和URL")
    void editsMetadataAndRenamesPairedFiles() throws Exception {
        String oldStem = "audio-旧歌名-68ec03f0-5e8f-4833-9030-1f0a3a6a39ed";
        Songs oldSong = storedSong(oldStem);
        when(songsMapper.selectById(8L)).thenReturn(oldSong);
        when(songsMapper.updateById(any(Songs.class))).thenReturn(1);
        byte[] oldCover = Files.readAllBytes(storage.resolve("covers").resolve(oldStem + "-cover-.png"));
        EditSongDTO dto = new EditSongDTO();
        dto.setSong_name("新歌 / remix? #1;100%");
        dto.setArtist("新歌手");
        dto.setAlbum("新专辑");
        dto.setLyrics("新歌词");
        dto.setT_lyrics("翻译歌词");

        songsWorkflow.editUploadSong(dto, 2L, 8L);

        String newStem = "audio-新歌 _ remix_ #1;100%-68ec03f0-5e8f-4833-9030-1f0a3a6a39ed";
        ArgumentCaptor<Songs> updated = ArgumentCaptor.forClass(Songs.class);
        verify(songsMapper).updateById(updated.capture());
        Songs song = updated.getValue();
        assertThat(song.getSongUrl()).contains("%23", "%3B", "%25");
        assertVersionedCoverUrl(song.getSongUrl(), "/static/songs/" + newStem + ".wav");
        assertVersionedCoverUrl(song.getSongCoverUrl(), "/static/song_covers/" + newStem + "-cover-.png");
        AudioFile audio = AudioFileIO.read(storage.resolve("songs").resolve(newStem + ".wav").toFile());
        assertThat(audio.getTag().getFirst(FieldKey.TITLE)).isEqualTo(dto.getSong_name());
        assertThat(audio.getTag().getFirst(FieldKey.ARTIST)).isEqualTo("新歌手");
        assertThat(audio.getTag().getFirst(FieldKey.ALBUM)).isEqualTo("新专辑");
        assertThat(audio.getTag().getFirst(FieldKey.LYRICS)).isEqualTo("新歌词");
        assertThat(song.getArtist()).isEqualTo("新歌手");
        assertThat(song.getAlbum()).isEqualTo("新专辑");
        assertThat(song.getTLyrics()).isEqualTo("翻译歌词");
        assertThat(song.getFileMd5()).isNotBlank();
        assertThat(song.getFileSize()).isEqualTo(Files.size(storage.resolve("songs").resolve(newStem + ".wav")));
        assertThat(Files.readAllBytes(storage.resolve("covers").resolve(newStem + "-cover-.png"))).isEqualTo(oldCover);
        assertDirectoryContains("songs", newStem + ".wav");
        assertDirectoryContains("covers", newStem + "-cover-.png");
    }

    @Test
    @DisplayName("清空歌词时删除音频内嵌歌词，保留文件名和其他标签")
    void clearsEmbeddedLyricsWithoutRenamingAudio() throws Exception {
        String stem = "audio-旧歌名-68ec03f0-5e8f-4833-9030-1f0a3a6a39ed";
        Songs song = storedSong(stem);
        when(songsMapper.selectById(8L)).thenReturn(song);
        when(songsMapper.updateById(any(Songs.class))).thenReturn(1);
        EditSongDTO dto = new EditSongDTO();
        dto.setLyrics("");

        songsWorkflow.editUploadSong(dto, 2L, 8L);

        AudioFile audio = AudioFileIO.read(storage.resolve("songs").resolve(stem + ".wav").toFile());
        assertThat(audio.getTag().getFirst(FieldKey.LYRICS)).isEmpty();
        assertThat(audio.getTag().getFirst(FieldKey.TITLE)).isEqualTo("旧歌名");
        assertThat(audio.getTag().getFirst(FieldKey.ARTIST)).isEqualTo("旧歌手");
        assertDirectoryContains("songs", stem + ".wav");
        assertDirectoryContains("covers", stem + "-cover-.png");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("数据库更新失败时恢复同名覆盖或改名后的音频和封面")
    void restoresFilesWhenMetadataUpdateFails(boolean renamed) throws Exception {
        String stem = "audio-旧歌名-68ec03f0-5e8f-4833-9030-1f0a3a6a39ed";
        Songs song = storedSong(stem);
        Path audioPath = storage.resolve("songs").resolve(stem + ".wav");
        Path coverPath = storage.resolve("covers").resolve(stem + "-cover-.png");
        byte[] oldAudio = Files.readAllBytes(audioPath);
        byte[] oldCover = Files.readAllBytes(coverPath);
        when(songsMapper.selectById(8L)).thenReturn(song);
        doThrow(new IllegalStateException("数据库写入失败")).when(songsMapper).updateById(any(Songs.class));
        EditSongDTO dto = new EditSongDTO();
        if (renamed) {
            dto.setSong_name("新歌名");
        }
        dto.setArtist("新歌手");
        dto.setSong_cover(image("png"));

        assertThrows(BusinessException.class, () -> songsWorkflow.editUploadSong(dto, 2L, 8L));

        assertThat(Files.readAllBytes(audioPath)).isEqualTo(oldAudio);
        assertThat(Files.readAllBytes(coverPath)).isEqualTo(oldCover);
        assertDirectoryContains("songs", stem + ".wav");
        assertDirectoryContains("covers", stem + "-cover-.png");
    }

    @ParameterizedTest
    @ValueSource(ints = {TransactionSynchronization.STATUS_COMMITTED, TransactionSynchronization.STATUS_ROLLED_BACK})
    @DisplayName("数据库事务最终提交才清理旧文件，回滚则恢复旧文件")
    void completesFileChangesWithDatabaseTransaction(int status) throws Exception {
        String oldStem = "audio-旧歌名-68ec03f0-5e8f-4833-9030-1f0a3a6a39ed";
        String newStem = "audio-新歌名-68ec03f0-5e8f-4833-9030-1f0a3a6a39ed";
        Songs song = storedSong(oldStem);
        when(songsMapper.selectById(8L)).thenReturn(song);
        when(songsMapper.updateById(any(Songs.class))).thenReturn(1);
        EditSongDTO dto = new EditSongDTO();
        dto.setSong_name("新歌名");
        TransactionSynchronizationManager.initSynchronization();
        try {
            songsWorkflow.editUploadSong(dto, 2L, 8L);
            assertThat(Files.exists(storage.resolve("songs").resolve(oldStem + ".wav"))).isTrue();
            assertThat(Files.exists(storage.resolve("songs").resolve(newStem + ".wav"))).isTrue();
            for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
                synchronization.afterCompletion(status);
            }
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
        String expected = status == TransactionSynchronization.STATUS_COMMITTED ? newStem : oldStem;
        assertDirectoryContains("songs", expected + ".wav");
        assertDirectoryContains("covers", expected + "-cover-.png");
    }

    @Test
    @DisplayName("目标文件冲突时拒绝改名并保留两个现有文件")
    void rejectsConflictingAudioTarget() throws Exception {
        String stem = "audio-旧歌名-68ec03f0-5e8f-4833-9030-1f0a3a6a39ed";
        Songs song = storedSong(stem);
        Path conflict = storage.resolve("songs").resolve("audio-新歌名-68ec03f0-5e8f-4833-9030-1f0a3a6a39ed.wav");
        byte[] unrelated = {7, 8, 9};
        Files.write(conflict, unrelated);
        when(songsMapper.selectById(8L)).thenReturn(song);
        EditSongDTO dto = new EditSongDTO();
        dto.setSong_name("新歌名");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> songsWorkflow.editUploadSong(dto, 2L, 8L));

        assertThat(exception.getCode()).isEqualTo(409);
        assertThat(Files.readAllBytes(conflict)).isEqualTo(unrelated);
        assertDirectoryContains("covers", stem + "-cover-.png");
        verify(songsMapper, never()).updateById(any(Songs.class));
    }

    private Songs editableSong(String stem, String extension) throws Exception {
        Songs song = new Songs();
        song.setSongId(8L);
        song.setUploaderId(2L);
        song.setSongUrl("/static/songs/" + stem + ".wav");
        song.setSongCoverUrl("/static/song_covers/" + stem + "-cover-." + extension + "?v=previous");
        Path songs = Files.createDirectories(storage.resolve("songs"));
        Files.write(songs.resolve(stem + ".wav"), wavAudio().getBytes());
        return song;
    }

    private Songs storedSong(String stem) throws Exception {
        Songs song = editableSong(stem, "png");
        song.setSongTitle("旧歌名");
        song.setArtist("旧歌手");
        song.setAlbum("旧专辑");
        song.setLyrics("旧歌词");
        AudioFile audio = AudioFileIO.read(storage.resolve("songs").resolve(stem + ".wav").toFile());
        var tag = audio.getTagOrCreateAndSetDefault();
        tag.setField(FieldKey.TITLE, song.getSongTitle());
        tag.setField(FieldKey.ARTIST, song.getArtist());
        tag.setField(FieldKey.ALBUM, song.getAlbum());
        tag.setField(FieldKey.LYRICS, song.getLyrics());
        audio.commit();
        Path covers = Files.createDirectories(storage.resolve("covers"));
        Files.write(covers.resolve(stem + "-cover-.png"), image("png").getBytes());
        return song;
    }

    private void assertDirectoryContains(String directory, String fileName) throws IOException {
        Path parent = storage.resolve(directory);
        try (Stream<Path> paths = Files.list(parent)) {
            assertThat(paths.toList()).containsExactly(parent.resolve(fileName));
        }
    }

    private MockMultipartFile wavAudio() throws Exception {
        AudioFormat format = new AudioFormat(8000, 16, 1, true, false);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (AudioInputStream input = new AudioInputStream(
                new ByteArrayInputStream(new byte[16000]), format, 8000)) {
            AudioSystem.write(input, AudioFileFormat.Type.WAVE, output);
        }
        return new MockMultipartFile("audiofile", "client.WAV", "audio/wav", output.toByteArray());
    }

    private MockMultipartFile image(String extension) throws IOException {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, extension, output);
        String mimeType = "png".equals(extension) ? "image/png" : "image/jpeg";
        return new MockMultipartFile("coverfile", "cover." + extension, mimeType, output.toByteArray());
    }

    private MockMultipartFile failingImage() throws IOException {
        return new MockMultipartFile("coverfile", "cover.png", "image/png", image("png").getBytes()) {
            @Override
            public void transferTo(File destination) throws IOException {
                Files.write(destination.toPath(), new byte[] {1});
                throw new IOException("文件写入失败");
            }
        };
    }

    private void assertStorageEmpty() throws IOException {
        try (Stream<Path> songs = Files.list(storage.resolve("songs"));
                Stream<Path> covers = Files.list(storage.resolve("covers"))) {
            assertThat(songs.toList()).isEmpty();
            assertThat(covers.toList()).isEmpty();
        }
    }

    private void assertVersionedCoverUrl(String url, String expectedPath) {
        url = UriUtils.decode(url, StandardCharsets.UTF_8);
        assertThat(url).startsWith(expectedPath + "?v=");
        String version = url.substring((expectedPath + "?v=").length());
        assertThat(version).matches("[0-9]{17}");
        LocalDateTime updatedAt = LocalDateTime.parse(version, DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        assertThat(updatedAt).isBetween(LocalDateTime.now().minusMinutes(1), LocalDateTime.now().plusMinutes(1));
    }
}
