package com.kyf.mp.server.modules.queue.repository.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.kyf.mp.server.common.repository.BaseRepositoryImpl;
import com.kyf.mp.server.modules.queue.entity.Queues;
import com.kyf.mp.server.modules.queue.mapper.QueuesMapper;
import com.kyf.mp.server.modules.queue.repository.QueuesRepository;

/** Queues 数据访问实现。 */
@Repository
public class QueuesRepositoryImpl extends BaseRepositoryImpl<QueuesMapper, Queues> implements QueuesRepository {

    public QueuesRepositoryImpl(QueuesMapper mapper) {
        super(mapper);
    }

    @Override
    public Queues findByOwner(Long queueId, Long userId) {
        return baseMapper.selectOne(new LambdaQueryWrapper<Queues>()
                .eq(Queues::getQueueId, queueId).eq(Queues::getCreatorId, userId));
    }

    @Override
    public List<Queues> findByCreatorOldestFirst(Long userId) {
        return baseMapper.selectList(new LambdaQueryWrapper<Queues>()
                .eq(Queues::getCreatorId, userId).orderByAsc(Queues::getCreatedDate));
    }

    @Override
    public Queues findLatestUpdated(Long userId) {
        return baseMapper.selectOne(new LambdaQueryWrapper<Queues>()
                .eq(Queues::getCreatorId, userId).orderByDesc(Queues::getUpdatedDate).last("LIMIT 1"));
    }

    @Override
    public Queues findLatestCreated(Long userId) {
        return baseMapper.selectOne(new LambdaQueryWrapper<Queues>()
                .eq(Queues::getCreatorId, userId).orderByDesc(Queues::getCreatedDate).last("LIMIT 1"));
    }

    @Override
    public Queues findOldestCreated(Long userId) {
        return baseMapper.selectOne(new LambdaQueryWrapper<Queues>()
                .eq(Queues::getCreatorId, userId).orderByAsc(Queues::getCreatedDate).last("LIMIT 1"));
    }

    @Override
    public long countByCreator(Long userId) {
        return baseMapper.selectCount(new LambdaQueryWrapper<Queues>().eq(Queues::getCreatorId, userId));
    }

    @Override
    public void clearCurrentFlags(Long userId) {
        baseMapper.update(null, new LambdaUpdateWrapper<Queues>()
                .set(Queues::getCurrent, false).eq(Queues::getCreatorId, userId));
    }

    @Override
    public void clearOtherCurrentFlags(Long userId, Long queueId) {
        baseMapper.update(null, new LambdaUpdateWrapper<Queues>()
                .set(Queues::getCurrent, false).eq(Queues::getCreatorId, userId)
                .ne(Queues::getQueueId, queueId));
    }

    @Override
    public void setCurrentFlag(Long userId, Long queueId) {
        baseMapper.update(null, new LambdaUpdateWrapper<Queues>()
                .set(Queues::getCurrent, true).eq(Queues::getQueueId, queueId)
                .eq(Queues::getCreatorId, userId));
    }
}
