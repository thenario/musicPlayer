package com.kyf.mp.server.modules.playlist.service;

import org.springframework.web.multipart.MultipartFile;

import com.kyf.mp.server.modules.playlist.vo.AddSongToPlaylistVO;
import com.kyf.mp.server.modules.playlist.vo.MyPlaylistsVO;
import com.kyf.mp.server.modules.playlist.vo.PlaylistActionVO;
import com.kyf.mp.server.modules.playlist.vo.PlaylistDetailVO;

/**
 * <p>
 * 服务类：业务逻辑层，不再继承 IService（基础 CRUD 由 business 层提供）。
 * </p>
 *
 * @author kyf
 * @since 2026-04-05
 */
public interface PlaylistsService {
        PlaylistActionVO createPlaylist(MultipartFile file, String name, String description,
                        Long userId);

        PlaylistActionVO editPlaylist(MultipartFile file, Long playlistId, String name,
                        String description, Long userId);

        void deletePlaylist(Long playlistId, Long userId);

        MyPlaylistsVO getMyPlaylists(Long userId);

        PlaylistDetailVO getPlaylistDetail(Long playlistId, Long userId);

        void toggleLike(Long playlistId, Long userId, boolean isLike);

        AddSongToPlaylistVO addSongToPlaylist(Long playlistId, Long songId, Long userId);

        void removeSongFromPlaylist(Long playlistId, Long songId, Long userId);
}
