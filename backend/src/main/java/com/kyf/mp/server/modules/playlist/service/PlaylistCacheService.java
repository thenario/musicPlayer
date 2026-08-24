package com.kyf.mp.server.modules.playlist.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.kyf.mp.server.modules.playlist.business.PlaylistsBusiness;
import com.kyf.mp.server.modules.playlist.vo.PlaylistContentVO;

import lombok.RequiredArgsConstructor;

/**
 * 歌单公共内容缓存。
 *
 * <p>独立 Bean 用于确保 {@link Cacheable} 通过 Spring 代理生效。</p>
 */
@Service
@RequiredArgsConstructor
public class PlaylistCacheService {

    private final PlaylistsBusiness playlistsBusiness;

    @Cacheable(cacheNames = "playlist-detail", key = "#playlistId")
    public PlaylistContentVO getPlaylistContent(Long playlistId) {
        return playlistsBusiness.getPlaylistContent(playlistId);
    }
}
