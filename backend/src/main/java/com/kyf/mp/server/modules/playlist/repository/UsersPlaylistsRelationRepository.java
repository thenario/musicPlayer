package com.kyf.mp.server.modules.playlist.repository;

import com.kyf.mp.server.modules.playlist.entity.UsersPlaylistsRelation;

/** UsersPlaylistsRelation 数据访问；业务规则由 Service 层负责。 */
public interface UsersPlaylistsRelationRepository {
    int insert(UsersPlaylistsRelation relation);
}
