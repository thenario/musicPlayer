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
import com.kyf.mp.server.modules.queue.repository.impl.PlayHistoryRepositoryImpl;
import com.kyf.mp.server.modules.queue.service.workflow.QueuesWorkflow;
import com.kyf.mp.server.modules.song.mapper.PlayHistoryMapper;
import com.kyf.mp.server.modules.song.repository.SongsRepository;
import com.kyf.mp.server.modules.song.service.workflow.SongsWorkflow;

@ExtendWith(MockitoExtension.class)
class SongsServiceImplTest {

    @Mock
    private SongsWorkflow songsWorkflow;

    @Mock
    private SongsRepository songsRepository;

    @Mock
    private PlayHistoryMapper playHistoryMapper;

    @Mock
    private QueuesWorkflow queuesWorkflow;

    private SongsServiceImpl songsService;

    @BeforeEach
    void setUp() {
        songsService = new SongsServiceImpl(songsWorkflow, songsRepository, new PlayHistoryRepositoryImpl(playHistoryMapper), queuesWorkflow);
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
