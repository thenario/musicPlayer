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
            Integer userId);

    PlaylistActionVO editPlaylist(MultipartFile file, Integer playlistId, String name,
            String description, Integer userId);

    void deletePlaylist(Integer playlistId, Integer userId);

    PlaylistDetailVO getPlaylistDetail(Integer playlistId, Integer userId);

    void toggleLike(Integer playlistId, Integer userId, boolean isLike);

    AddSongToPlaylistVO addSongToPlaylist(Integer playlistId, Integer songId);

    void removeSongFromPlaylist(Integer playlistId, Integer songId);
}
