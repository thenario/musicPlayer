package com.kyf.mp.server.modules.playlist.repository;

import com.kyf.mp.server.modules.playlist.entity.UsersLikeplaylistsRelation;

/** UsersLikeplaylistsRelation 数据访问；业务规则由 Service 层负责。 */
public interface UsersLikeplaylistsRelationRepository {
    int countByUserAndPlaylist(Long userId, Long playlistId);
    int insertIgnore(UsersLikeplaylistsRelation relation);
    int deleteByUserAndPlaylist(Long userId, Long playlistId);
}
