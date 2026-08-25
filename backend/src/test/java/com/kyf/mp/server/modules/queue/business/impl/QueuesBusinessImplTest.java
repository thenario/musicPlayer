package com.kyf.mp.server.modules.queue.business.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.kyf.mp.server.modules.playlist.entity.Playlists;
import com.kyf.mp.server.modules.playlist.entity.SongsPlaylistsRelation;
import com.kyf.mp.server.modules.playlist.mapper.PlaylistsMapper;
import com.kyf.mp.server.modules.playlist.mapper.SongsPlaylistsRelationMapper;
import com.kyf.mp.server.modules.queue.entity.PlayState;
import com.kyf.mp.server.modules.queue.entity.QueueItems;
import com.kyf.mp.server.modules.song.entity.Songs;
import com.kyf.mp.server.modules.queue.entity.Queues;
import com.kyf.mp.server.modules.queue.mapper.PlayStateMapper;
import com.kyf.mp.server.modules.queue.mapper.QueueCustomMapper;
import com.kyf.mp.server.modules.queue.mapper.QueueItemsMapper;
import com.kyf.mp.server.modules.queue.mapper.QueuesMapper;
import com.kyf.mp.server.modules.queue.vo.CreateQueueFromPlaylist;
import com.kyf.mp.server.modules.song.mapper.SongsMapper;

@ExtendWith(MockitoExtension.class)
class QueuesBusinessImplTest {

    @Mock
    private QueueCustomMapper queueCustomMapper;
    @Mock
    private PlayStateMapper playStateMapper;
    @Mock
    private QueueItemsMapper queueItemsMapper;
    @Mock
    private QueuesMapper queuesMapper;
    @Mock
    private PlaylistsMapper playlistsMapper;
    @Mock
    private SongsMapper songsMapper;
    @Mock
    private SongsPlaylistsRelationMapper songsPlaylistsRelationMapper;
    @InjectMocks
    private QueuesBusinessImpl queuesBusiness;

    @BeforeEach
    void configureBusinessDefaults() {
        ReflectionTestUtils.setField(queuesBusiness, "maxQueuesPerUser", 5);
    }

    @Test
    @DisplayName("从歌单创建队列时，应按原歌单顺序写入队列项")
    void copiesPlaylistItemsThroughQueueItemMapper() {
        Playlists playlist = new Playlists();
        playlist.setPlaylistName("My playlist");
        playlist.setCreatorId(7L);
        playlist.setPubliclyVisible(false);

        SongsPlaylistsRelation first = relation(11L, 1);
        SongsPlaylistsRelation second = relation(12L, 2);
        when(playStateMapper.selectOne(any(Wrapper.class))).thenReturn(null);
        when(queuesMapper.selectList(any(Wrapper.class))).thenReturn(List.of());
        when(playlistsMapper.selectById(99L)).thenReturn(playlist);
        when(songsPlaylistsRelationMapper.findByPlaylistId(99L)).thenReturn(List.of(first, second));
        when(queuesMapper.insert(any(Queues.class))).thenAnswer(invocation -> {
            invocation.getArgument(0, Queues.class).setQueueId(123L);
            return 1;
        });

        CreateQueueFromPlaylist result = queuesBusiness.createQueueFromPlaylist(7L, 99L);

        ArgumentCaptor<QueueItems> items = ArgumentCaptor.forClass(QueueItems.class);
        verify(queueItemsMapper, org.mockito.Mockito.times(2)).insert(items.capture());
        assertThat(items.getAllValues())
                .extracting(QueueItems::getQueueId, QueueItems::getSongId, QueueItems::getQueueItemPosition)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(123L, 11L, 1),
                        org.assertj.core.groups.Tuple.tuple(123L, 12L, 2));
        assertThat(result.getQueueId()).isEqualTo(123L);
        assertThat(result.getSongCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("重新添加已存在歌曲时，应先关闭旧位置且不增加歌曲数量")
    void reinsertedSongClosesItsOldPositionBeforeInsertion() {
        Queues queue = new Queues();
        queue.setQueueId(123L);
        PlayState playState = new PlayState();
        playState.setCurrentPosition(3);
        QueueItems existingItem = new QueueItems();
        existingItem.setQueueItemId(456L);
        existingItem.setQueueItemPosition(1);

        when(songsMapper.selectById(11L)).thenReturn(new Songs());
        when(queuesMapper.selectOne(any(Wrapper.class))).thenReturn(queue);
        when(playStateMapper.selectOne(any(Wrapper.class))).thenReturn(playState);
        when(queueItemsMapper.selectOne(any(Wrapper.class))).thenReturn(existingItem);
        when(queueItemsMapper.insert(any(QueueItems.class))).thenAnswer(invocation -> {
            invocation.getArgument(0, QueueItems.class).setQueueItemId(789L);
            return 1;
        });

        com.kyf.mp.server.modules.queue.dto.AddSongToQueue request = new com.kyf.mp.server.modules.queue.dto.AddSongToQueue();
        request.setSongId(11L);
        request.setMode(true);
        queuesBusiness.addSongToQueue(7L, 123L, request);

        verify(queueItemsMapper).deleteById(456L);
        verify(queueCustomMapper).shiftPositionsDown(123L, 1);
        verify(queueCustomMapper).moveItemPositionsToTemporary(123L, 3);
        verify(queueCustomMapper).restoreShiftedItemPositions(123L, 3);
        verify(queueCustomMapper, org.mockito.Mockito.never()).incrementSongCount(123L);
    }

    @Test
    @DisplayName("删除当前歌曲之前的项目时，应同步播放位置")
    void deletingItemBeforeCurrentSongSynchronizesPlaybackPosition() {
        Long userId = 7L;
        Long queueId = 123L;
        Long deletedItemId = 456L;

        // Arrange：模拟删除队列第 1 首歌，而当前播放的是另一首歌。
        when(queueCustomMapper.selectItemDetailForDelete(deletedItemId, userId)).thenReturn(Map.of(
                "queue_id", queueId,
                "queue_item_position", 1,
                "song_id", 11L));
        PlayState playState = new PlayState();
        playState.setCurrentSongId(22L);
        when(playStateMapper.selectOne(any(Wrapper.class))).thenReturn(playState);

        // Act：执行真实业务实现，所有 Mapper 都是 mock，不会访问数据库。
        queuesBusiness.removeSongFromQueue(userId, queueId, deletedItemId);

        // Assert：业务层应删除项目、将后续项目位置左移，并按当前歌曲重新同步位置。
        verify(queueItemsMapper).deleteById(deletedItemId);
        verify(queueCustomMapper).shiftPositionsDown(queueId, 1);
        verify(queueCustomMapper).syncPlayStatePosition(userId, queueId);
        verify(queueCustomMapper).decrementSongCount(queueId);
    }

    private SongsPlaylistsRelation relation(Long songId, int position) {
        SongsPlaylistsRelation relation = new SongsPlaylistsRelation();
        relation.setSongId(songId);
        relation.setSongPlaylistPosition(position);
        return relation;
    }
}
