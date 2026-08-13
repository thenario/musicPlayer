package com.kyf.mp.javaserver.modules.playlistmodule.service.serviceImp;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.kyf.mp.javaserver.common.BusinessException;
import com.kyf.mp.javaserver.modules.playlistmodule.business.PlaylistsBusiness;
import com.kyf.mp.javaserver.modules.playlistmodule.entity.Playlists;
import com.kyf.mp.javaserver.modules.playlistmodule.service.PlaylistsService;
import com.kyf.mp.javaserver.modules.playlistmodule.vo.AddSongToPlaylistVO;
import com.kyf.mp.javaserver.modules.playlistmodule.vo.MyPlaylistsVO;
import com.kyf.mp.javaserver.modules.playlistmodule.vo.PlaylistActionVO;
import com.kyf.mp.javaserver.modules.playlistmodule.vo.PlaylistDetailVO;
import com.kyf.mp.javaserver.modules.playlistmodule.vo.PlaylistSummaryVO;

import lombok.RequiredArgsConstructor;

/**
 * <p>
 * 服务实现类：业务逻辑编排，数据访问委托给 PlaylistsBusiness。
 * </p>
 *
 * @author kyf
 * @since 2026-04-05
 */
@Service
@RequiredArgsConstructor
public class PlaylistsServiceImpl implements PlaylistsService {

    private final PlaylistsBusiness playlistsBusiness;

    @Override
    public PlaylistActionVO createPlaylist(MultipartFile file, String name, String description,
            Long userId) {
        return playlistsBusiness.createPlaylist(file, name, description, userId);
    }

    @Override
    public PlaylistActionVO editPlaylist(MultipartFile file, Long playlistId, String name,
            String description, Long userId) {
        return playlistsBusiness.editPlaylist(file, playlistId, name, description, userId);
    }

    @Override
    public void deletePlaylist(Long playlistId, Long userId) {
        playlistsBusiness.deletePlaylist(playlistId, userId);
    }

    @Override
    public MyPlaylistsVO getMyPlaylists(Long userId) {
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
        return vo;
    }

    @Override
    public PlaylistDetailVO getPlaylistDetail(Long playlistId, Long userId) {
        return playlistsBusiness.getPlaylistDetail(playlistId, userId);
    }

    @Override
    public void toggleLike(Long playlistId, Long userId, boolean isLike) {
        playlistsBusiness.toggleLike(playlistId, userId, isLike);
    }

    @Override
    public AddSongToPlaylistVO addSongToPlaylist(Long playlistId, Long songId) {
        return playlistsBusiness.addSongToPlaylist(playlistId, songId);
    }

    @Override
    public void removeSongFromPlaylist(Long playlistId, Long songId) {
        playlistsBusiness.removeSongFromPlaylist(playlistId, songId);
    }
}
