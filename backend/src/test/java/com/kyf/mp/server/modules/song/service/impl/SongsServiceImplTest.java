package com.kyf.mp.server.modules.song.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kyf.mp.server.modules.queue.entity.PlayHistory;
import com.kyf.mp.server.modules.queue.business.QueuesBusiness;
import com.kyf.mp.server.modules.song.business.SongsBusiness;
import com.kyf.mp.server.modules.song.mapper.PlayHistoryMapper;

@ExtendWith(MockitoExtension.class)
class SongsServiceImplTest {

    @Mock
    private SongsBusiness songsBusiness;

    @Mock
    private PlayHistoryMapper playHistoryMapper;

    @Mock
    private QueuesBusiness queuesBusiness;

    private SongsServiceImpl songsService;

    @BeforeEach
    void setUp() {
        songsService = new SongsServiceImpl(songsBusiness, playHistoryMapper, queuesBusiness);
    }

    @Test
    void repeatedSongUpdatesExistingHistoryInsteadOfInsertingAnotherRow() {
        PlayHistory existing = new PlayHistory();
        existing.setHistoryId(10L);
        existing.setUserId(1L);
        existing.setSongId(2L);

        when(playHistoryMapper.selectList(any()))
                .thenReturn(List.of(existing), List.of(existing));

        songsService.syncPlayHistory(1L, 2L);

        verify(playHistoryMapper).updateById(existing);
        verify(playHistoryMapper, never()).insert(any(PlayHistory.class));
        verify(playHistoryMapper, never()).deleteBatchIds(any());
    }

    @Test
    void firstPlayInsertsHistory() {
        when(playHistoryMapper.selectList(any()))
                .thenReturn(List.of(), List.of());

        songsService.syncPlayHistory(1L, 2L);

        verify(playHistoryMapper).insert(any(PlayHistory.class));
    }
}
