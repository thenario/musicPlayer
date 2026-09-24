package com.kyf.mp.server.modules.playlist.repository.impl;

import org.springframework.stereotype.Repository;

import com.kyf.mp.server.modules.playlist.entity.UsersPlaylistsRelation;
import com.kyf.mp.server.modules.playlist.mapper.UsersPlaylistsRelationMapper;
import com.kyf.mp.server.modules.playlist.repository.UsersPlaylistsRelationRepository;

/** UsersPlaylistsRelation 数据访问实现。 */
@Repository
public class UsersPlaylistsRelationRepositoryImpl implements UsersPlaylistsRelationRepository {
    private final UsersPlaylistsRelationMapper mapper;

    public UsersPlaylistsRelationRepositoryImpl(UsersPlaylistsRelationMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public int insert(UsersPlaylistsRelation relation) {
        return mapper.insert(relation);
    }
}
