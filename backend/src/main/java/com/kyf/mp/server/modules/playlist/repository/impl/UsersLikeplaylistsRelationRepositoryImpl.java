package com.kyf.mp.server.modules.playlist.repository.impl;

import org.springframework.stereotype.Repository;

import com.kyf.mp.server.modules.playlist.entity.UsersLikeplaylistsRelation;
import com.kyf.mp.server.modules.playlist.mapper.UsersLikeplaylistsRelationMapper;
import com.kyf.mp.server.modules.playlist.repository.UsersLikeplaylistsRelationRepository;

/** UsersLikeplaylistsRelation 数据访问实现。 */
@Repository
public class UsersLikeplaylistsRelationRepositoryImpl implements UsersLikeplaylistsRelationRepository {
    private final UsersLikeplaylistsRelationMapper mapper;

    public UsersLikeplaylistsRelationRepositoryImpl(UsersLikeplaylistsRelationMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public int countByUserAndPlaylist(Long userId, Long playlistId) {
        return mapper.countByUserAndPlaylist(userId, playlistId);
    }

    @Override
    public int insertIgnore(UsersLikeplaylistsRelation relation) {
        return mapper.insertIgnore(relation);
    }

    @Override
    public int deleteByUserAndPlaylist(Long userId, Long playlistId) {
        return mapper.deleteByUserAndPlaylist(userId, playlistId);
    }
}
