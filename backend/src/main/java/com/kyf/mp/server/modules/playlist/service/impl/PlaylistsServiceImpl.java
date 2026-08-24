package com.kyf.mp.server.modules.playlist.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.kyf.mp.server.common.BusinessException;
import com.kyf.mp.server.modules.playlist.business.PlaylistsBusiness;
import com.kyf.mp.server.modules.playlist.entity.Playlists;
import com.kyf.mp.server.modules.playlist.service.PlaylistCacheService;
import com.kyf.mp.server.modules.playlist.service.PlaylistsService;
import com.kyf.mp.server.modules.playlist.vo.AddSongToPlaylistVO;
import com.kyf.mp.server.modules.playlist.vo.MyPlaylistsVO;
import com.kyf.mp.server.modules.playlist.vo.PlaylistActionVO;
import com.kyf.mp.server.modules.playlist.vo.PlaylistContentVO;
import com.kyf.mp.server.modules.playlist.vo.PlaylistDetailVO;
import com.kyf.mp.server.modules.playlist.vo.PlaylistSummaryVO;

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
    private final PlaylistCacheService playlistCacheService;

    @Override
    @CacheEvict(cacheNames = "user-playlists", key = "#userId")
    public PlaylistActionVO createPlaylist(MultipartFile file, String name, String description,
            Long userId) {
        return playlistsBusiness.createPlaylist(file, name, description, userId);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "playlist-detail", key = "#playlistId"),
            @CacheEvict(cacheNames = "user-playlists", key = "#userId")
    })
    public PlaylistActionVO editPlaylist(MultipartFile file, Long playlistId, String name,
            String description, Long userId) {
        return playlistsBusiness.editPlaylist(file, playlistId, name, description, userId);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "playlist-detail", key = "#playlistId"),
            @CacheEvict(cacheNames = "user-playlists", key = "#userId")
    })
    public void deletePlaylist(Long playlistId, Long userId) {
        playlistsBusiness.deletePlaylist(playlistId, userId);
    }

    @Override
    @Cacheable(cacheNames = "user-playlists", key = "#userId")
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
        playlistsBusiness.assertCanViewPlaylist(playlistId, userId);
        PlaylistContentVO content = playlistCacheService.getPlaylistContent(playlistId);

        PlaylistDetailVO vo = new PlaylistDetailVO();
        vo.setPlaylist(content.getPlaylist());
        vo.setSongs(content.getSongs());
        vo.setLiked(playlistsBusiness.isPlaylistLiked(playlistId, userId));
        return vo;
    }

    @Override
    @CacheEvict(cacheNames = "playlist-detail", key = "#playlistId")
    public void toggleLike(Long playlistId, Long userId, boolean isLike) {
        playlistsBusiness.toggleLike(playlistId, userId, isLike);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "playlist-detail", key = "#playlistId"),
            @CacheEvict(cacheNames = "user-playlists", key = "#userId")
    })
    public AddSongToPlaylistVO addSongToPlaylist(Long playlistId, Long songId, Long userId) {
        return playlistsBusiness.addSongToPlaylist(playlistId, songId, userId);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "playlist-detail", key = "#playlistId"),
            @CacheEvict(cacheNames = "user-playlists", key = "#userId")
    })
    public void removeSongFromPlaylist(Long playlistId, Long songId, Long userId) {
        playlistsBusiness.removeSongFromPlaylist(playlistId, songId, userId);
    }
}
