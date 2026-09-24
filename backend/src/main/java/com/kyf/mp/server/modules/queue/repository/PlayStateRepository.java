package com.kyf.mp.server.modules.queue.repository;

import com.kyf.mp.server.common.repository.BaseRepository;
import com.kyf.mp.server.modules.queue.entity.PlayState;

/** PlayState 数据访问；业务规则由 Service 层负责。 */
public interface PlayStateRepository extends BaseRepository<PlayState> {
    PlayState findByUser(Long userId);
    PlayState findByUserAndQueue(Long userId, Long queueId);
}
