package com.kyf.mp.javaserver.modules.playlistmodule.business;

import org.springframework.web.multipart.MultipartFile;

import com.kyf.mp.javaserver.common.business.IBaseBusiness;
import com.kyf.mp.javaserver.modules.playlistmodule.entity.Playlists;
import com.kyf.mp.javaserver.modules.playlistmodule.vo.AddSongToPlaylistVO;
import com.kyf.mp.javaserver.modules.playlistmodule.vo.PlaylistActionVO;
import com.kyf.mp.javaserver.modules.playlistmodule.vo.PlaylistDetailVO;

/**
 * 歌单数据访问层：复杂数据库操作在此定义。
 */
public interface IPlaylistsBusiness extends IBaseBusiness<Playlists> {
    PlaylistActionVO createPlaylist(MultipartFile file, String name, String description,
            Long userId);

    PlaylistActionVO editPlaylist(MultipartFile file, Long playlistId, String name,
            String description, Long userId);

    void deletePlaylist(Long playlistId, Long userId);

    PlaylistDetailVO getPlaylistDetail(Long playlistId, Long userId);

    void toggleLike(Long playlistId, Long userId, boolean isLike);

    AddSongToPlaylistVO addSongToPlaylist(Long playlistId, Long songId);

    void removeSongFromPlaylist(Long playlistId, Long songId);
}
