package com.kyf.mp.server.modules.queue.repository.impl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.kyf.mp.server.modules.queue.mapper.QueueCustomMapper;
import com.kyf.mp.server.modules.queue.repository.QueueCustomRepository;
import com.kyf.mp.server.modules.queue.vo.CurrentQueueVO;
import com.kyf.mp.server.modules.queue.vo.ReturnQueueVO;

/** QueueCustom 数据访问实现。 */
@Repository
public class QueueCustomRepositoryImpl implements QueueCustomRepository {
    private final QueueCustomMapper mapper;

    public QueueCustomRepositoryImpl(QueueCustomMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<CurrentQueueVO> selectCurrentQueueDetail(Long userId) {
        return mapper.selectCurrentQueueDetail(userId);
    }

    @Override
    public List<ReturnQueueVO> selectMyQueues(Long userId) {
        return mapper.selectMyQueues(userId);
    }

    @Override
    public ReturnQueueVO selectQueueById(Long queueId, Long userId) {
        return mapper.selectQueueById(queueId, userId);
    }

    @Override
    public void moveItemPositionsToTemporary(Long queueId, int pos) {
        mapper.moveItemPositionsToTemporary(queueId, pos);
    }

    @Override
    public void restoreShiftedItemPositions(Long queueId, int pos) {
        mapper.restoreShiftedItemPositions(queueId, pos);
    }

    @Override
    public void incrementSongCount(Long queueId) {
        mapper.incrementSongCount(queueId);
    }

    @Override
    public void decrementSongCount(Long queueId) {
        mapper.decrementSongCount(queueId);
    }

    @Override
    public void shiftPositionsDown(Long queueId, Integer pos) {
        mapper.shiftPositionsDown(queueId, pos);
    }

    @Override
    public Map<String, Object> selectItemDetailForDelete(Long itemId, Long userId) {
        return mapper.selectItemDetailForDelete(itemId, userId);
    }

    @Override
    public void batchUpdatePositions(Long queueId, List<Long> songIds) {
        mapper.batchUpdatePositions(queueId, songIds);
    }

    @Override
    public void moveAllItemPositionsToTemporary(Long queueId) {
        mapper.moveAllItemPositionsToTemporary(queueId);
    }

    @Override
    public void syncPlayStatePosition(Long userId, Long queueId) {
        mapper.syncPlayStatePosition(userId, queueId);
    }
}
