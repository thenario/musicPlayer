package com.kyf.mp.server.modules.song.repository;

import java.util.List;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kyf.mp.server.common.repository.BaseRepository;
import com.kyf.mp.server.modules.song.entity.Songs;

/** Songs 数据访问；业务规则由 Service 层负责。 */
public interface SongsRepository extends BaseRepository<Songs> {
    Page<Songs> findPage(Page<Songs> page, String keyword);
    Songs findLyrics(Long songId);
    Page<Songs> findUploads(Long userId, Page<Songs> page);
    Songs findUpload(Long userId, Long songId);
    List<Songs> findPlaylistSongs(List<Long> songIds);
    List<Songs> findScannerSongs(Long uploaderId);
}
