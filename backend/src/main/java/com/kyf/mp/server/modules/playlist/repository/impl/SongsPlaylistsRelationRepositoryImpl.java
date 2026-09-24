package com.kyf.mp.server.modules.playlist.repository.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.kyf.mp.server.modules.playlist.entity.SongsPlaylistsRelation;
import com.kyf.mp.server.modules.playlist.mapper.SongsPlaylistsRelationMapper;
import com.kyf.mp.server.modules.playlist.repository.SongsPlaylistsRelationRepository;

/** SongsPlaylistsRelation 数据访问实现。 */
@Repository
public class SongsPlaylistsRelationRepositoryImpl implements SongsPlaylistsRelationRepository {
    private final SongsPlaylistsRelationMapper mapper;

    public SongsPlaylistsRelationRepositoryImpl(SongsPlaylistsRelationMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<SongsPlaylistsRelation> findByPlaylistId(Long playlistId) {
        return mapper.findByPlaylistId(playlistId);
    }

    @Override
    public SongsPlaylistsRelation findByPlaylistAndSong(Long playlistId, Long songId) {
        return mapper.findByPlaylistAndSong(playlistId, songId);
    }

    @Override
    public Integer getMaxPosition(Long playlistId) {
        return mapper.getMaxPosition(playlistId);
    }

    @Override
    public int insert(SongsPlaylistsRelation relation) {
        return mapper.insert(relation);
    }

    @Override
    public int deleteByPlaylistAndSong(Long playlistId, Long songId) {
        return mapper.deleteByPlaylistAndSong(playlistId, songId);
    }

    @Override
    public int decrementPositionsAfter(Long playlistId, Integer position) {
        return mapper.decrementPositionsAfter(playlistId, position);
    }
}
