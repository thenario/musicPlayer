package com.kyf.mp.javaserver.modules.playlistmodule.business;

import org.springframework.web.multipart.MultipartFile;

import com.kyf.mp.javaserver.common.ResultModel;
import com.kyf.mp.javaserver.common.business.IBaseBusiness;
import com.kyf.mp.javaserver.modules.playlistmodule.entity.Playlists;
import com.kyf.mp.javaserver.modules.playlistmodule.vo.AddSongToPlaylistVO;
import com.kyf.mp.javaserver.modules.playlistmodule.vo.PlaylistActionVO;
import com.kyf.mp.javaserver.modules.playlistmodule.vo.PlaylistDetailVO;

/**
 * 歌单数据访问层：复杂数据库操作在此定义。
 */
public interface IPlaylistsBusiness extends IBaseBusiness<Playlists> {
    ResultModel<PlaylistActionVO> createPlaylist(MultipartFile file, String name, String description,
            Integer userId);

    ResultModel<PlaylistActionVO> editPlaylist(MultipartFile file, Integer playlistId, String name,
            String description, Integer userId);

    ResultModel<Void> deletePlaylist(Integer playlistId, Integer userId);

    ResultModel<PlaylistDetailVO> getPlaylistDetail(Integer playlistId, Integer userId);

    ResultModel<Void> toggleLike(Integer playlistId, Integer userId, boolean isLike);

    ResultModel<AddSongToPlaylistVO> addSongToPlaylist(Integer playlistId, Integer songId);

    ResultModel<Void> removeSongFromPlaylist(Integer playlistId, Integer songId);
}
