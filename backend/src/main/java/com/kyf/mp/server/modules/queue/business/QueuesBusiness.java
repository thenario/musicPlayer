package com.kyf.mp.server.modules.queue.business;

import java.util.List;

import com.kyf.mp.server.common.business.BaseBusiness;
import com.kyf.mp.server.modules.queue.dto.AddSongToQueue;
import com.kyf.mp.server.modules.queue.dto.UpdateCurrentQueueStateDTO;
import com.kyf.mp.server.modules.queue.entity.Queues;
import com.kyf.mp.server.modules.queue.vo.AddSongToQueueVO;
import com.kyf.mp.server.modules.queue.vo.AlterQueueVO;
import com.kyf.mp.server.modules.queue.vo.CreateQueueFromPlaylist;
import com.kyf.mp.server.modules.queue.vo.CurrentQueue;
import com.kyf.mp.server.modules.queue.vo.DeleteQueueVO;
import com.kyf.mp.server.modules.queue.vo.MyQueues;
import com.kyf.mp.server.modules.queue.vo.SingleQueue;

/**
 * 队列数据访问层：复杂数据库操作在此定义。
 */
public interface QueuesBusiness extends BaseBusiness<Queues> {
    CurrentQueue getCurrentQueue(Long userId);

    MyQueues getMyQueues(Long userId);

    SingleQueue getQueueById(Long queueId);

    DeleteQueueVO deleteQueue(Long userId, Long queueId);

    void clearQueue(Long userId, Long queueId);

    CreateQueueFromPlaylist createQueueFromPlaylist(Long userId, Long playlistId);

    AddSongToQueueVO addSongToQueue(Long userId, Long paramQueueId, AddSongToQueue dto);

    void removeSongFromQueue(Long userId, Long queueId, Long queueItemId);

    void updateCurrentQueueState(Long userId, UpdateCurrentQueueStateDTO wrapper);

    AlterQueueVO alterQueueToCurrent(Long userId, Long queueId);

    void setPlayMode(Long userId, Long queueId, String playMode);

    void reorderQueue(Long userId, Long queueId, List<Long> songIds);
}
