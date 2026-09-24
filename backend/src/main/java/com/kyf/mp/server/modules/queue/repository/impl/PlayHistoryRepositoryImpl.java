package com.kyf.mp.server.modules.queue.repository.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kyf.mp.server.common.repository.BaseRepositoryImpl;
import com.kyf.mp.server.modules.queue.entity.PlayHistory;
import com.kyf.mp.server.modules.queue.repository.PlayHistoryRepository;
import com.kyf.mp.server.modules.song.mapper.PlayHistoryMapper;
import com.kyf.mp.server.modules.song.vo.PlayHistoryVO;

/** PlayHistory 数据访问实现。 */
@Repository
public class PlayHistoryRepositoryImpl extends BaseRepositoryImpl<PlayHistoryMapper, PlayHistory> implements PlayHistoryRepository {

    public PlayHistoryRepositoryImpl(PlayHistoryMapper mapper) {
        super(mapper);
    }

    @Override
    public List<PlayHistory> findByUserAndSongNewestFirst(Long userId, Long songId) {
        return baseMapper.selectList(new LambdaQueryWrapper<PlayHistory>()
                .eq(PlayHistory::getUserId, userId).eq(PlayHistory::getSongId, songId)
                .orderByDesc(PlayHistory::getPlayedDate).orderByDesc(PlayHistory::getHistoryId));
    }

    @Override
    public List<PlayHistory> findByUserOldestFirst(Long userId) {
        return baseMapper.selectList(new LambdaQueryWrapper<PlayHistory>()
                .eq(PlayHistory::getUserId, userId)
                .orderByAsc(PlayHistory::getPlayedDate).orderByAsc(PlayHistory::getHistoryId));
    }

    @Override
    public List<PlayHistory> findByUserNewestFirst(Long userId) {
        return baseMapper.selectList(new LambdaQueryWrapper<PlayHistory>()
                .eq(PlayHistory::getUserId, userId)
                .orderByDesc(PlayHistory::getPlayedDate).orderByDesc(PlayHistory::getHistoryId));
    }

    @Override
    public List<PlayHistoryVO> selectHistoryWithSong(Long userId) {
        return baseMapper.selectHistoryWithSong(userId);
    }
}
