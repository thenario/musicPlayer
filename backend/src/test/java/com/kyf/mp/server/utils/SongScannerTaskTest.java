package com.kyf.mp.server.utils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kyf.mp.server.modules.song.entity.Songs;
import com.kyf.mp.server.modules.song.mapper.SongsMapper;
import com.kyf.mp.server.modules.user.entity.Users;
import com.kyf.mp.server.modules.user.mapper.UsersMapper;

@ExtendWith(MockitoExtension.class)
class SongScannerTaskTest {

    @TempDir
    Path songsDirectory;

    @Mock
    private SongsMapper songsMapper;

    @Mock
    private UsersMapper usersMapper;

    private SongScannerTask scanner;

    @BeforeEach
    void setUp() {
        scanner = new SongScannerTask(songsMapper, usersMapper);
        ReflectionTestUtils.setField(scanner, "songPath", songsDirectory.toString());
        ReflectionTestUtils.setField(scanner, "songCoverPath", songsDirectory.resolve("covers").toString());
        ReflectionTestUtils.setField(scanner, "songUrlPrefix", "/static/songs/");
        ReflectionTestUtils.setField(scanner, "songCoverUrlPrefix", "/static/song_covers/");
        ReflectionTestUtils.setField(scanner, "scannerUploaderId", 7L);
        when(usersMapper.selectById(7L)).thenReturn(new Users());
    }

    @Test
    void doesNotDeleteMissingSongsByDefault() {
        when(songsMapper.selectList(any(QueryWrapper.class))).thenReturn(List.of(song(1L, "/static/songs/missing.mp3")));

        scanner.runTask();

        verify(songsMapper, never()).deleteBatchIds(any());
    }

    @Test
    void deletesMissingSongsOnlyWhenExplicitlyEnabled() {
        ReflectionTestUtils.setField(scanner, "removeMissingSongs", true);
        when(songsMapper.selectList(any(QueryWrapper.class))).thenReturn(List.of(song(1L, "/static/songs/missing.mp3")));

        scanner.runTask();

        verify(songsMapper).deleteBatchIds(List.of(1L));
    }

    @Test
    void doesNotInsertSongAlreadyKnownByItsStaticUrl() throws IOException {
        Files.createFile(songsDirectory.resolve("existing.mp3"));
        when(songsMapper.selectList(any(QueryWrapper.class))).thenReturn(List.of(song(1L, "/static/songs/existing.mp3")));

        scanner.runTask();

        verify(songsMapper, never()).insert(any(Songs.class));
    }

    private Songs song(Long id, String url) {
        Songs song = new Songs();
        song.setSongId(id);
        song.setSongUrl(url);
        return song;
    }
}
