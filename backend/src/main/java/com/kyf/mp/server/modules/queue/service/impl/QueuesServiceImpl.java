package com.kyf.mp.server.modules.queue.service.impl;

import java.util.List;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import com.kyf.mp.server.modules.queue.service.workflow.QueuesWorkflow;
import com.kyf.mp.server.modules.queue.dto.AddSongToQueueDTO;
import com.kyf.mp.server.modules.queue.dto.UpdateCurrentQueueStateDTO;
import com.kyf.mp.server.modules.queue.service.QueuesService;
import com.kyf.mp.server.modules.queue.vo.AddSongToQueueVO;
import com.kyf.mp.server.modules.queue.vo.AlterQueueVO;
import com.kyf.mp.server.modules.queue.vo.CreateQueueFromPlaylistVO;
import com.kyf.mp.server.modules.queue.vo.CurrentQueueVO;
import com.kyf.mp.server.modules.queue.vo.DeleteQueueVO;
import com.kyf.mp.server.modules.queue.vo.MyQueuesVO;
import com.kyf.mp.server.modules.queue.vo.SingleQueueVO;

import lombok.RequiredArgsConstructor;

/**
 * <p>
 * 服务实现类：业务逻辑编排，业务流程委托给 QueuesWorkflow。
 * </p>
 *
 * @author kyf
 * @since 2026-04-05
 */
@Service
@RequiredArgsConstructor
public class QueuesServiceImpl implements QueuesService {

    private final QueuesWorkflow queuesWorkflow;

    @Override
    @Cacheable(cacheNames = "user-queues", key = "#userId + ':' +'current'")
    public CurrentQueueVO getCurrentQueue(Long userId) {
        return queuesWorkflow.getCurrentQueue(userId);
    }

    @Override
    @Cacheable(cacheNames = "user-queues", key = "#userId + ':' + 'all-queues'")
    public MyQueuesVO getMyQueues(Long userId) {
        return queuesWorkflow.getMyQueues(userId);
    }

    @Override
    @Cacheable(cacheNames = "user-queues", key = "#userId + ':' + #queueId")
    public SingleQueueVO getQueueById(Long userId, Long queueId) {
        return queuesWorkflow.getQueueById(userId, queueId);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "user-queues", key = "#userId + ':' + 'all-queues'"),
            @CacheEvict(cacheNames = "user-queues", key = "#userId + ':'+ 'current'"),
            @CacheEvict(cacheNames = "user-queues", key = "#userId + ':' + #queueId"),
    })
    public DeleteQueueVO deleteQueue(Long userId, Long queueId) {
        return queuesWorkflow.deleteQueue(userId, queueId);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "user-queues", key = "#userId + ':' +'current'"),
            @CacheEvict(cacheNames = "user-queues", key = "#userId + ':' + 'all-queues'"),
            @CacheEvict(cacheNames = "user-queues", key = "#userId + ':' + #queueId"),
    })
    public void clearQueue(Long userId, Long queueId) {
        queuesWorkflow.clearQueue(userId, queueId);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "user-queues", key = "#userId + ':' + 'all-queues'"),
            @CacheEvict(cacheNames = "user-queues", key = "#userId + ':'+ 'current'")
    })
    public CreateQueueFromPlaylistVO createQueueFromPlaylist(Long userId, Long playlistId) {
        return queuesWorkflow.createQueueFromPlaylist(userId, playlistId);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "user-queues", key = "#userId + ':' +'current'"),
            @CacheEvict(cacheNames = "user-queues", key = "#userId + ':' + 'all-queues'"),
            @CacheEvict(cacheNames = "user-queues", key = "#userId + ':' + #paramQueueId"),
    })
    public AddSongToQueueVO addSongToQueue(Long userId, Long paramQueueId, AddSongToQueueDTO dto) {
        return queuesWorkflow.addSongToQueue(userId, paramQueueId, dto);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "user-queues", key = "#userId + ':' +'current'"),
            @CacheEvict(cacheNames = "user-queues", key = "#userId + ':' + 'all-queues'"),
            @CacheEvict(cacheNames = "user-queues", key = "#userId + ':' + #queueId"),
    })
    public void removeSongFromQueue(Long userId, Long queueId, Long queueItemId) {
        queuesWorkflow.removeSongFromQueue(userId, queueId, queueItemId);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "user-queues", key = "#userId + ':'+ 'current'"),
    })
    public void updateCurrentQueueState(Long userId, UpdateCurrentQueueStateDTO wrapper) {
        queuesWorkflow.updateCurrentQueueState(userId, wrapper);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "user-queues", key = "#userId + ':' +'current'"),
            @CacheEvict(cacheNames = "user-queues", key = "#userId + ':' + 'all-queues'"),

    })
    public AlterQueueVO alterQueueToCurrent(Long userId, Long queueId) {
        return queuesWorkflow.alterQueueToCurrent(userId, queueId);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "user-queues", key = "#userId + ':'+ 'current'"),
    })
    public void setPlayMode(Long userId, Long queueId, String playMode) {
        queuesWorkflow.setPlayMode(userId, queueId, playMode);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "user-queues", key = "#userId + ':' +'current'"),
            @CacheEvict(cacheNames = "user-queues", key = "#userId + ':' + 'all-queues'"),
            @CacheEvict(cacheNames = "user-queues", key = "#userId + ':' + #queueId"),
    })
    public void reorderQueue(Long userId, Long queueId, List<Long> songIds) {
        queuesWorkflow.reorderQueue(userId, queueId, songIds);
    }
}
