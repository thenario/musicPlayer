package com.kyf.mp.server.modules.queue.repository;

import java.util.List;
import java.util.Map;

import com.kyf.mp.server.modules.queue.vo.CurrentQueueVO;
import com.kyf.mp.server.modules.queue.vo.ReturnQueueVO;

/** QueueCustom 数据访问；业务规则由 Service 层负责。 */
public interface QueueCustomRepository {
    List<CurrentQueueVO> selectCurrentQueueDetail(Long userId);
    List<ReturnQueueVO> selectMyQueues(Long userId);
    ReturnQueueVO selectQueueById(Long queueId, Long userId);
    void moveItemPositionsToTemporary(Long queueId, int pos);
    void restoreShiftedItemPositions(Long queueId, int pos);
    void incrementSongCount(Long queueId);
    void decrementSongCount(Long queueId);
    void shiftPositionsDown(Long queueId, Integer pos);
    Map<String, Object> selectItemDetailForDelete(Long itemId, Long userId);
    void batchUpdatePositions(Long queueId, List<Long> songIds);
    void moveAllItemPositionsToTemporary(Long queueId);
    void syncPlayStatePosition(Long userId, Long queueId);
}
