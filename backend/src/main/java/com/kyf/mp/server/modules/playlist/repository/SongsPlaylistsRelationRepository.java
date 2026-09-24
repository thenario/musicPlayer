package com.kyf.mp.server.modules.playlist.repository;

import java.util.List;

import com.kyf.mp.server.modules.playlist.entity.SongsPlaylistsRelation;

/** SongsPlaylistsRelation 数据访问；业务规则由 Service 层负责。 */
public interface SongsPlaylistsRelationRepository {
    List<SongsPlaylistsRelation> findByPlaylistId(Long playlistId);
    SongsPlaylistsRelation findByPlaylistAndSong(Long playlistId, Long songId);
    Integer getMaxPosition(Long playlistId);
    int insert(SongsPlaylistsRelation relation);
    int deleteByPlaylistAndSong(Long playlistId, Long songId);
    int decrementPositionsAfter(Long playlistId, Integer position);
}
