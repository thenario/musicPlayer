package com.kyf.mp.server.modules.queue.repository;

import java.util.List;

import com.kyf.mp.server.common.repository.BaseRepository;
import com.kyf.mp.server.modules.queue.entity.QueueItems;

/** QueueItems 数据访问；业务规则由 Service 层负责。 */
public interface QueueItemsRepository extends BaseRepository<QueueItems> {
    int deleteByQueue(Long queueId);
    List<QueueItems> findByQueue(Long queueId);
    QueueItems findByQueueAndSong(Long queueId, Long value);
    QueueItems findFirstByQueueAndSong(Long queueId, Long value);
    QueueItems findByQueueAndPosition(Long queueId, Integer value);
}
