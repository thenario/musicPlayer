package com.kyf.mp.server.modules.playlist.repository;

import java.util.List;

import com.kyf.mp.server.common.repository.BaseRepository;
import com.kyf.mp.server.modules.playlist.entity.Playlists;

/** Playlists 数据访问；业务规则由 Service 层负责。 */
public interface PlaylistsRepository extends BaseRepository<Playlists> {
    List<Playlists> findByCreator(Long userId);
    void incrementLikeCount(Long playlistId);
    void decrementLikeCount(Long playlistId);
    void incrementSongCount(Long playlistId);
    void decrementSongCount(Long playlistId);
}
