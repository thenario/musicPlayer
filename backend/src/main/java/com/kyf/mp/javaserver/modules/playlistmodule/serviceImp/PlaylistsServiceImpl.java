package com.kyf.mp.javaserver.modules.playlistmodule.serviceImp;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.kyf.mp.javaserver.common.BusinessException;
import com.kyf.mp.javaserver.common.ResultModel;
import com.kyf.mp.javaserver.modules.playlistmodule.business.IPlaylistsBusiness;
import com.kyf.mp.javaserver.modules.playlistmodule.entity.Playlists;
import com.kyf.mp.javaserver.modules.playlistmodule.service.IPlaylistsService;
import com.kyf.mp.javaserver.modules.playlistmodule.vo.AddSongToPlaylistVO;
import com.kyf.mp.javaserver.modules.playlistmodule.vo.MyPlaylistsVO;
import com.kyf.mp.javaserver.modules.playlistmodule.vo.PlaylistActionVO;
import com.kyf.mp.javaserver.modules.playlistmodule.vo.PlaylistDetailVO;
import com.kyf.mp.javaserver.modules.playlistmodule.vo.PlaylistSummaryVO;

import lombok.RequiredArgsConstructor;

/**
 * <p>
 * 服务实现类：业务逻辑编排，数据访问委托给 IPlaylistsBusiness。
 * </p>
 *
 * @author kyf
 * @since 2026-04-05
 */
@Service
@RequiredArgsConstructor
public class PlaylistsServiceImpl implements IPlaylistsService {

    private final IPlaylistsBusiness playlistsBusiness;

    @Override
    public ResultModel<PlaylistActionVO> createPlaylist(MultipartFile file, String name, String description,
            Integer userId) {
        return playlistsBusiness.createPlaylist(file, name, description, userId);
    }

    @Override
    public ResultModel<PlaylistActionVO> editPlaylist(MultipartFile file, Integer playlistId, String name,
            String description, Integer userId) {
        return playlistsBusiness.editPlaylist(file, playlistId, name, description, userId);
    }

    @Override
    public ResultModel<Void> deletePlaylist(Integer playlistId, Integer userId) {
        return playlistsBusiness.deletePlaylist(playlistId, userId);
    }

    @Override
    public ResultModel<MyPlaylistsVO> getMyPlaylists(Integer userId) {
        if (userId == null)
            throw new BusinessException(401, "请先登录");

        // 简单查询：直接用 business 的基础 CRUD
        List<Playlists> list = playlistsBusiness.lambdaQuery()
                .eq(Playlists::getCreatorId, userId)
                .orderByDesc(Playlists::getCreatedDate)
                .list();

        List<PlaylistSummaryVO> playlistVOList = list.stream().map(p -> {
            PlaylistSummaryVO pVO = new PlaylistSummaryVO();
            BeanUtils.copyProperties(p, pVO);
            return pVO;
        }).collect(Collectors.toList());

        MyPlaylistsVO vo = new MyPlaylistsVO();
        vo.setPlaylists(playlistVOList);
        return ResultModel.success(vo);
    }

    @Override
    public ResultModel<PlaylistDetailVO> getPlaylistDetail(Integer playlistId, Integer userId) {
        return playlistsBusiness.getPlaylistDetail(playlistId, userId);
    }

    @Override
    public ResultModel<Void> toggleLike(Integer playlistId, Integer userId, boolean isLike) {
        return playlistsBusiness.toggleLike(playlistId, userId, isLike);
    }

    @Override
    public ResultModel<AddSongToPlaylistVO> addSongToPlaylist(Integer playlistId, Integer songId) {
        return playlistsBusiness.addSongToPlaylist(playlistId, songId);
    }

    @Override
    public ResultModel<Void> removeSongFromPlaylist(Integer playlistId, Integer songId) {
        return playlistsBusiness.removeSongFromPlaylist(playlistId, songId);
    }
}
