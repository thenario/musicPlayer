package com.kyf.mp.server.modules.queue.repository.impl;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kyf.mp.server.common.repository.BaseRepositoryImpl;
import com.kyf.mp.server.modules.queue.entity.PlayState;
import com.kyf.mp.server.modules.queue.mapper.PlayStateMapper;
import com.kyf.mp.server.modules.queue.repository.PlayStateRepository;

/** PlayState 数据访问实现。 */
@Repository
public class PlayStateRepositoryImpl extends BaseRepositoryImpl<PlayStateMapper, PlayState> implements PlayStateRepository {

    public PlayStateRepositoryImpl(PlayStateMapper mapper) {
        super(mapper);
    }

    @Override
    public PlayState findByUser(Long userId) {
        return baseMapper.selectOne(new LambdaQueryWrapper<PlayState>().eq(PlayState::getUserId, userId));
    }

    @Override
    public PlayState findByUserAndQueue(Long userId, Long queueId) {
        return baseMapper.selectOne(new LambdaQueryWrapper<PlayState>()
                .eq(PlayState::getUserId, userId).eq(PlayState::getCurrentQueueId, queueId));
    }
}
