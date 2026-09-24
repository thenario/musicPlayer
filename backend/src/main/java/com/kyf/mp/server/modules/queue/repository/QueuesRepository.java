package com.kyf.mp.server.modules.queue.repository;

import java.util.List;

import com.kyf.mp.server.common.repository.BaseRepository;
import com.kyf.mp.server.modules.queue.entity.Queues;

/** Queues 数据访问；业务规则由 Service 层负责。 */
public interface QueuesRepository extends BaseRepository<Queues> {
    Queues findByOwner(Long queueId, Long userId);
    List<Queues> findByCreatorOldestFirst(Long userId);
    Queues findLatestUpdated(Long userId);
    Queues findLatestCreated(Long userId);
    Queues findOldestCreated(Long userId);
    long countByCreator(Long userId);
    void clearCurrentFlags(Long userId);
    void clearOtherCurrentFlags(Long userId, Long queueId);
    void setCurrentFlag(Long userId, Long queueId);
}
