package com.kyf.mp.server.modules.playlist.business.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.session.Configuration;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.kyf.mp.server.common.BusinessException;
import com.kyf.mp.server.modules.playlist.entity.Playlists;
import com.kyf.mp.server.modules.playlist.entity.SongsPlaylistsRelation;
import com.kyf.mp.server.modules.playlist.entity.UsersLikeplaylistsRelation;
import com.kyf.mp.server.modules.playlist.mapper.PlaylistsMapper;
import com.kyf.mp.server.modules.playlist.mapper.SongsPlaylistsRelationMapper;
import com.kyf.mp.server.modules.playlist.mapper.UsersLikeplaylistsRelationMapper;
import com.kyf.mp.server.modules.playlist.mapper.UsersPlaylistsRelationMapper;
import com.kyf.mp.server.modules.playlist.vo.AddSongToPlaylistVO;
import com.kyf.mp.server.modules.playlist.vo.PlaylistContentVO;
import com.kyf.mp.server.modules.song.entity.Songs;
import com.kyf.mp.server.modules.song.mapper.SongsMapper;

@ExtendWith(MockitoExtension.class)
class PlaylistsBusinessImplTest {

    @Mock
    private PlaylistsMapper playlistsMapper;
    @Mock
    private SongsMapper songsMapper;
    @Mock
    private UsersPlaylistsRelationMapper userPlaylistMapper;
    @Mock
    private UsersLikeplaylistsRelationMapper likeRelationMapper;
    @Mock
    private SongsPlaylistsRelationMapper songsPlaylistsRelationMapper;

    private PlaylistsBusinessImpl playlistsBusiness;

    @BeforeEach
    void setUp() {
        initTableInfo(Playlists.class);
        initTableInfo(Songs.class);
        playlistsBusiness = new PlaylistsBusinessImpl(songsMapper, userPlaylistMapper,
                likeRelationMapper, songsPlaylistsRelationMapper);
        ReflectionTestUtils.setField(playlistsBusiness, "baseMapper", playlistsMapper);
        ReflectionTestUtils.setField(playlistsBusiness, "playlistCoverPath", "target/test-playlist-covers");
        ReflectionTestUtils.setField(playlistsBusiness, "coverUrlPrefix", "/static/playlist-covers/");
    }

    private void initTableInfo(Class<?> entityType) {
        if (TableInfoHelper.getTableInfo(entityType) == null) {
            TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new Configuration(), ""), entityType);
        }
    }

    @Test
    @DisplayName("查看公开歌单时允许访问")
    void allowsViewingPublicPlaylist() {
        Playlists playlist = playlist(10L, 20L, true);
        when(playlistsMapper.selectById(10L)).thenReturn(playlist);

        assertThatCode(() -> playlistsBusiness.assertCanViewPlaylist(10L, 99L))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("查看不存在歌单时抛出404")
    void rejectsMissingPlaylist() {
        when(playlistsMapper.selectById(10L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> playlistsBusiness.assertCanViewPlaylist(10L, 99L));

        assertThat(exception.getCode()).isEqualTo(404);
    }

    @Test
    @DisplayName("查看私密歌单且不是创建者时抛出403")
    void rejectsPrivatePlaylistForOtherUser() {
        when(playlistsMapper.selectById(10L)).thenReturn(playlist(10L, 20L, false));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> playlistsBusiness.assertCanViewPlaylist(10L, 99L));

        assertThat(exception.getCode()).isEqualTo(403);
    }

    @Test
    @DisplayName("查询歌单内容时按关联关系返回歌曲顺序")
    void returnsPlaylistContentInRelationOrder() {
        Playlists playlist = playlist(10L, 20L, true);
        SongsPlaylistsRelation relation = new SongsPlaylistsRelation();
        relation.setPlaylistId(10L);
        relation.setSongId(30L);
        relation.setSongPlaylistPosition(2);
        Songs song = new Songs();
        song.setSongId(30L);
        song.setSongTitle("测试歌曲");

        when(playlistsMapper.selectById(10L)).thenReturn(playlist);
        when(songsPlaylistsRelationMapper.findByPlaylistId(10L)).thenReturn(List.of(relation));
        when(songsMapper.selectList(any(Wrapper.class))).thenReturn(List.of(song));

        PlaylistContentVO result = playlistsBusiness.getPlaylistContent(10L);

        assertThat(result.getPlaylist()).isSameAs(playlist);
        assertThat(result.getSongs()).hasSize(1);
        assertThat(result.getSongs().get(0).getSongId()).isEqualTo(30L);
        assertThat(result.getSongs().get(0).getSongPlaylistPosition()).isEqualTo(2);
    }

    @Test
    @DisplayName("用户未登录时不认为歌单已点赞")
    void anonymousUserCannotLikePlaylist() {
        assertThat(playlistsBusiness.isPlaylistLiked(10L, null)).isFalse();
        verify(likeRelationMapper, never()).countByUserAndPlaylist(any(), any());
    }

    @Test
    @DisplayName("新增歌曲到歌单时写入下一个位置")
    void addsSongAtNextPosition() {
        when(playlistsMapper.selectById(10L)).thenReturn(playlist(10L, 20L, true));
        when(songsMapper.selectById(30L)).thenReturn(new Songs());
        when(songsPlaylistsRelationMapper.getMaxPosition(10L)).thenReturn(4);

        AddSongToPlaylistVO result = playlistsBusiness.addSongToPlaylist(10L, 30L, 20L);

        ArgumentCaptor<SongsPlaylistsRelation> captor =
                ArgumentCaptor.forClass(SongsPlaylistsRelation.class);
        verify(songsPlaylistsRelationMapper).insert(captor.capture());
        assertThat(captor.getValue().getPlaylistId()).isEqualTo(10L);
        assertThat(captor.getValue().getSongId()).isEqualTo(30L);
        assertThat(captor.getValue().getSongPlaylistPosition()).isEqualTo(5);
        assertThat(result.getPosition()).isEqualTo(5);
        verify(playlistsMapper).update(any(), any(Wrapper.class));
    }

    @Test
    @DisplayName("移除歌单歌曲时删除关联并调整后续位置")
    void removesSongAndShiftsFollowingPositions() {
        when(playlistsMapper.selectById(10L)).thenReturn(playlist(10L, 20L, true));
        SongsPlaylistsRelation relation = new SongsPlaylistsRelation();
        relation.setSongPlaylistPosition(3);
        when(songsPlaylistsRelationMapper.findByPlaylistAndSong(10L, 30L)).thenReturn(relation);

        playlistsBusiness.removeSongFromPlaylist(10L, 30L, 20L);

        verify(songsPlaylistsRelationMapper).deleteByPlaylistAndSong(10L, 30L);
        verify(songsPlaylistsRelationMapper).decrementPositionsAfter(10L, 3);
        verify(playlistsMapper).update(any(), any(Wrapper.class));
    }

    private Playlists playlist(Long playlistId, Long creatorId, boolean visible) {
        Playlists playlist = new Playlists();
        playlist.setPlaylistId(playlistId);
        playlist.setCreatorId(creatorId);
        playlist.setPubliclyVisible(visible);
        return playlist;
    }
}
