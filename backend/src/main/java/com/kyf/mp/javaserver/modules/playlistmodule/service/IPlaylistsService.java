package com.kyf.mp.javaserver.modules.playlistmodule.service;

import org.springframework.web.multipart.MultipartFile;

import com.kyf.mp.javaserver.modules.playlistmodule.vo.AddSongToPlaylistVO;
import com.kyf.mp.javaserver.modules.playlistmodule.vo.MyPlaylistsVO;
import com.kyf.mp.javaserver.modules.playlistmodule.vo.PlaylistActionVO;
import com.kyf.mp.javaserver.modules.playlistmodule.vo.PlaylistDetailVO;

/**
 * <p>
 * 服务类：业务逻辑层，不再继承 IService（基础 CRUD 由 business 层提供）。
 * </p>
 *
 * @author kyf
 * @since 2026-04-05
 */
public interface IPlaylistsService {
    PlaylistActionVO createPlaylist(MultipartFile file, String name, String description,
            Integer userId);

    PlaylistActionVO editPlaylist(MultipartFile file, Integer playlistId, String name,
            String description, Integer userId);

    void deletePlaylist(Integer playlistId, Integer userId);

    MyPlaylistsVO getMyPlaylists(Integer userId);

    PlaylistDetailVO getPlaylistDetail(Integer playlistId, Integer userId);

    void toggleLike(Integer playlistId, Integer userId, boolean isLike);

    AddSongToPlaylistVO addSongToPlaylist(Integer playlistId, Integer songId);

    void removeSongFromPlaylist(Integer playlistId, Integer songId);
}
