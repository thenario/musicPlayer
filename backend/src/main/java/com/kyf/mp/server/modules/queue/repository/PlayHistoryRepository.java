package com.kyf.mp.server.modules.queue.repository;

import java.util.List;

import com.kyf.mp.server.common.repository.BaseRepository;
import com.kyf.mp.server.modules.queue.entity.PlayHistory;
import com.kyf.mp.server.modules.song.vo.PlayHistoryVO;

/** PlayHistory 数据访问；业务规则由 Service 层负责。 */
public interface PlayHistoryRepository extends BaseRepository<PlayHistory> {
    List<PlayHistory> findByUserAndSongNewestFirst(Long userId, Long songId);
    List<PlayHistory> findByUserOldestFirst(Long userId);
    List<PlayHistory> findByUserNewestFirst(Long userId);
    List<PlayHistoryVO> selectHistoryWithSong(Long userId);
}
