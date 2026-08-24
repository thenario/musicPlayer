package com.kyf.mp.server.modules.queue.service.impl;

import java.util.List;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import com.kyf.mp.server.modules.queue.business.QueuesBusiness;
import com.kyf.mp.server.modules.queue.dto.AddSongToQueue;
import com.kyf.mp.server.modules.queue.dto.UpdateCurrentQueueStateDTO;
import com.kyf.mp.server.modules.queue.service.QueuesService;
import com.kyf.mp.server.modules.queue.vo.AddSongToQueueVO;
import com.kyf.mp.server.modules.queue.vo.AlterQueueVO;
import com.kyf.mp.server.modules.queue.vo.CreateQueueFromPlaylist;
import com.kyf.mp.server.modules.queue.vo.CurrentQueue;
import com.kyf.mp.server.modules.queue.vo.DeleteQueueVO;
import com.kyf.mp.server.modules.queue.vo.MyQueues;
import com.kyf.mp.server.modules.queue.vo.SingleQueue;

import lombok.RequiredArgsConstructor;

/**
 * <p>
 * 服务实现类：业务逻辑编排，数据访问委托给 QueuesBusiness。
 * </p>
 *
 * @author kyf
 * @since 2026-04-05
 */
@Service
@RequiredArgsConstructor
public class QueuesServiceImpl implements QueuesService {

    private final QueuesBusiness queuesBusiness;

    @Override
    @Cacheable(cacheNames = "user-queues", key = "#userId + ':' +'current'")
    public CurrentQueue getCurrentQueue(Long userId) {
        return queuesBusiness.getCurrentQueue(userId);
    }

    @Override
    @Cacheable(cacheNames = "user-queues", key = "#userId + ':' + 'all-queues'")
    public MyQueues getMyQueues(Long userId) {
        return queuesBusiness.getMyQueues(userId);
    }

    @Override
    @Cacheable(cacheNames = "user-queues", key = "#userId + ':' + #queueId")
    public SingleQueue getQueueById(Long userId, Long queueId) {
        return queuesBusiness.getQueueById(userId, queueId);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "user-queues", key = "#userId + ':' + 'all-queues'"),
            @CacheEvict(cacheNames = "user-queues", key = "#userId + ':'+ 'current'"),
            @CacheEvict(cacheNames = "user-queues", key = "#userId + ':' + #queueId"),
    })
    public DeleteQueueVO deleteQueue(Long userId, Long queueId) {
        return queuesBusiness.deleteQueue(userId, queueId);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "user-queues", key = "#userId + ':' +'current'"),
            @CacheEvict(cacheNames = "user-queues", key = "#userId + ':' + 'all-queues'"),
            @CacheEvict(cacheNames = "user-queues", key = "#userId + ':' + #queueId"),
    })
    public void clearQueue(Long userId, Long queueId) {
        queuesBusiness.clearQueue(userId, queueId);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "user-queues", key = "#userId + ':' + 'all-queues'"),
            @CacheEvict(cacheNames = "user-queues", key = "#userId + ':'+ 'current'")
    })
    public CreateQueueFromPlaylist createQueueFromPlaylist(Long userId, Long playlistId) {
        return queuesBusiness.createQueueFromPlaylist(userId, playlistId);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "user-queues", key = "#userId + ':' +'current'"),
            @CacheEvict(cacheNames = "user-queues", key = "#userId + ':' + 'all-queues'"),
            @CacheEvict(cacheNames = "user-queues", key = "#userId + ':' + #paramQueueId"),
    })
    public AddSongToQueueVO addSongToQueue(Long userId, Long paramQueueId, AddSongToQueue dto) {
        return queuesBusiness.addSongToQueue(userId, paramQueueId, dto);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "user-queues", key = "#userId + ':' +'current'"),
            @CacheEvict(cacheNames = "user-queues", key = "#userId + ':' + 'all-queues'"),
            @CacheEvict(cacheNames = "user-queues", key = "#userId + ':' + #queueId"),
    })
    public void removeSongFromQueue(Long userId, Long queueId, Long queueItemId) {
        queuesBusiness.removeSongFromQueue(userId, queueId, queueItemId);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "user-queues", key = "#userId + ':'+ 'current'"),
    })
    public void updateCurrentQueueState(Long userId, UpdateCurrentQueueStateDTO wrapper) {
        queuesBusiness.updateCurrentQueueState(userId, wrapper);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "user-queues", key = "#userId + ':' +'current'"),
            @CacheEvict(cacheNames = "user-queues", key = "#userId + ':' + 'all-queues'"),

    })
    public AlterQueueVO alterQueueToCurrent(Long userId, Long queueId) {
        return queuesBusiness.alterQueueToCurrent(userId, queueId);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "user-queues", key = "#userId + ':'+ 'current'"),
    })
    public void setPlayMode(Long userId, Long queueId, String playMode) {
        queuesBusiness.setPlayMode(userId, queueId, playMode);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "user-queues", key = "#userId + ':' +'current'"),
            @CacheEvict(cacheNames = "user-queues", key = "#userId + ':' + 'all-queues'"),
            @CacheEvict(cacheNames = "user-queues", key = "#userId + ':' + #queueId"),
    })
    public void reorderQueue(Long userId, Long queueId, List<Long> songIds) {
        queuesBusiness.reorderQueue(userId, queueId, songIds);
    }
}
