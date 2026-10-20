package com.kyf.mp.server.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.util.UriUtils;

import com.kyf.mp.server.modules.song.repository.SongsRepository;
import com.kyf.mp.server.modules.user.repository.UsersRepository;

class SongScannerUrlTest {
    @TempDir
    Path directory;

    @Test
    @DisplayName("扫描生成系统日期版本URL，读取数据库时还原真实文件路径")
    void scansVersionedUrlAndResolvesOriginalPath() throws Exception {
        Path path = directory.resolve("audio-24⧸7 #100%-test.wav");
        AudioFormat format = new AudioFormat(8000, 16, 1, true, false);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (AudioInputStream input = new AudioInputStream(
                new ByteArrayInputStream(new byte[16000]), format, 8000)) {
            AudioSystem.write(input, AudioFileFormat.Type.WAVE, output);
        }
        Files.write(path, output.toByteArray());
        var audio = AudioFileIO.read(path.toFile());
        audio.getTagOrCreateAndSetDefault().setField(FieldKey.TITLE, "24⧸7 #100%");
        audio.commit();
        SongsRepository repository = mock(SongsRepository.class);
        SongScannerTask scanner = new SongScannerTask(repository, mock(UsersRepository.class));
        ReflectionTestUtils.setField(scanner, "songPath", directory.toString());
        ReflectionTestUtils.setField(scanner, "songUrlPrefix", "/static/songs/");

        var scanned = scanner.scanSongsFromDir(2L, "上传者");

        assertThat(scanned).hasSize(1);
        assertThat(scanned.getFirst()).containsKey(path);
        var song = scanned.getFirst().get(path);
        String url = song.getSongUrl();
        String prefix = "/static/songs/" + UriUtils.encode(path.getFileName().toString(), StandardCharsets.UTF_8) + "?v=";
        assertThat(url).startsWith(prefix);
        String version = url.substring(prefix.length());
        assertThat(version).matches("[0-9]{17}");
        LocalDateTime scannedAt = LocalDateTime.parse(version, DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        assertThat(scannedAt).isBetween(LocalDateTime.now().minusMinutes(1), LocalDateTime.now().plusMinutes(1));
        verifyNoInteractions(repository);

        when(repository.findAll()).thenReturn(List.of(song));
        assertThat(scanner.scanSongsFromSql().getFirst()).containsKey(path);
    }
}
