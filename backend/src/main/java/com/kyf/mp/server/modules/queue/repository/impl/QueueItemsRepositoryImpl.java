package com.kyf.mp.server.modules.queue.repository.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kyf.mp.server.common.repository.BaseRepositoryImpl;
import com.kyf.mp.server.modules.queue.entity.QueueItems;
import com.kyf.mp.server.modules.queue.mapper.QueueItemsMapper;
import com.kyf.mp.server.modules.queue.repository.QueueItemsRepository;

/** QueueItems 数据访问实现。 */
@Repository
public class QueueItemsRepositoryImpl extends BaseRepositoryImpl<QueueItemsMapper, QueueItems> implements QueueItemsRepository {

    public QueueItemsRepositoryImpl(QueueItemsMapper mapper) {
        super(mapper);
    }

    @Override
    public int deleteByQueue(Long queueId) {
        return baseMapper.delete(new LambdaQueryWrapper<QueueItems>().eq(QueueItems::getQueueId, queueId));
    }

    @Override
    public List<QueueItems> findByQueue(Long queueId) {
        return baseMapper.selectList(new LambdaQueryWrapper<QueueItems>().eq(QueueItems::getQueueId, queueId));
    }

    @Override
    public QueueItems findByQueueAndSong(Long queueId, Long value) {
        return baseMapper.selectOne(new LambdaQueryWrapper<QueueItems>()
                .eq(QueueItems::getQueueId, queueId).eq(QueueItems::getSongId, value));
    }

    @Override
    public QueueItems findFirstByQueueAndSong(Long queueId, Long value) {
        return baseMapper.selectOne(new LambdaQueryWrapper<QueueItems>()
                .eq(QueueItems::getQueueId, queueId).eq(QueueItems::getSongId, value).last("LIMIT 1"));
    }

    @Override
    public QueueItems findByQueueAndPosition(Long queueId, Integer value) {
        return baseMapper.selectOne(new LambdaQueryWrapper<QueueItems>()
                .eq(QueueItems::getQueueId, queueId).eq(QueueItems::getQueueItemPosition, value).last("LIMIT 1"));
    }
}
