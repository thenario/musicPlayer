package com.kyf.mp.javaserver.service;

import com.kyf.mp.javaserver.common.ResultModel;
import com.kyf.mp.javaserver.entity.Playlists;
import com.kyf.mp.javaserver.modelVO.AddSongToPlaylistVO;
import com.kyf.mp.javaserver.modelVO.MyPlaylistsVO;
import com.kyf.mp.javaserver.modelVO.PlaylistActionVO;
import com.kyf.mp.javaserver.modelVO.PlaylistDetailVO;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author kyf
 * @since 2026-04-05
 */
@Service
public interface IPlaylistsService extends IService<Playlists> {
    ResultModel<PlaylistActionVO> createPlaylist(MultipartFile file, String name, String description,
            Integer userId);

    ResultModel<PlaylistActionVO> editPlaylist(MultipartFile file, Integer playlistId, String name,
            String description, Integer userId);

    ResultModel<Void> deletePlaylist(Integer playlistId, Integer userId);

    ResultModel<MyPlaylistsVO> getMyPlaylists(Integer userId);

    ResultModel<PlaylistDetailVO> getPlaylistDetail(Integer playlistId, Integer userId);

    ResultModel<Void> toggleLike(Integer playlistId, Integer userId, boolean isLike);

    ResultModel<AddSongToPlaylistVO> addSongToPlaylist(Integer playlistId, Integer songId);

    ResultModel<Void> removeSongFromPlaylist(Integer playlistId, Integer songId);
}
