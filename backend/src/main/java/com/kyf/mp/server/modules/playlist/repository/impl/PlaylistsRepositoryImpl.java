package com.kyf.mp.server.modules.playlist.repository.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kyf.mp.server.common.repository.BaseRepositoryImpl;
import com.kyf.mp.server.modules.playlist.entity.Playlists;
import com.kyf.mp.server.modules.playlist.mapper.PlaylistsMapper;
import com.kyf.mp.server.modules.playlist.repository.PlaylistsRepository;

/** Playlists 数据访问实现。 */
@Repository
public class PlaylistsRepositoryImpl extends BaseRepositoryImpl<PlaylistsMapper, Playlists> implements PlaylistsRepository {

    public PlaylistsRepositoryImpl(PlaylistsMapper mapper) {
        super(mapper);
    }

    @Override
    public List<Playlists> findByCreator(Long userId) {
        return baseMapper.selectList(new LambdaQueryWrapper<Playlists>()
                .eq(Playlists::getCreatorId, userId).orderByDesc(Playlists::getCreatedDate));
    }

    @Override
    public void incrementLikeCount(Long playlistId) {
        update().setSql("like_count = like_count + 1").eq("playlist_id", playlistId).update();
    }

    @Override
    public void decrementLikeCount(Long playlistId) {
        update().setSql("like_count = GREATEST(like_count - 1, 0)").eq("playlist_id", playlistId).update();
    }

    @Override
    public void incrementSongCount(Long playlistId) {
        update().setSql("song_count = song_count + 1").eq("playlist_id", playlistId).update();
    }

    @Override
    public void decrementSongCount(Long playlistId) {
        update().setSql("song_count = GREATEST(song_count - 1, 0)").eq("playlist_id", playlistId).update();
    }
}
