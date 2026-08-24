package com.kyf.mp.server.modules.playlist.business;

import org.springframework.web.multipart.MultipartFile;

import com.kyf.mp.server.common.business.BaseBusiness;
import com.kyf.mp.server.modules.playlist.entity.Playlists;
import com.kyf.mp.server.modules.playlist.vo.AddSongToPlaylistVO;
import com.kyf.mp.server.modules.playlist.vo.PlaylistActionVO;
import com.kyf.mp.server.modules.playlist.vo.PlaylistDetailVO;

/**
 * 歌单数据访问层：复杂数据库操作在此定义。
 */
public interface PlaylistsBusiness extends BaseBusiness<Playlists> {
        PlaylistActionVO createPlaylist(MultipartFile file, String name, String description,
                        Long userId);

        PlaylistActionVO editPlaylist(MultipartFile file, Long playlistId, String name,
                        String description, Long userId);

        void deletePlaylist(Long playlistId, Long userId);

        PlaylistDetailVO getPlaylistDetail(Long playlistId, Long userId);

        void toggleLike(Long playlistId, Long userId, boolean isLike);

        AddSongToPlaylistVO addSongToPlaylist(Long playlistId, Long songId, Long userId);

        void removeSongFromPlaylist(Long playlistId, Long songId, Long userId);
}
