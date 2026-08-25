package com.kyf.mp.server.modules.queue.business;

import java.util.List;

import com.kyf.mp.server.common.business.BaseBusiness;
import com.kyf.mp.server.modules.queue.dto.AddSongToQueueDTO;
import com.kyf.mp.server.modules.queue.dto.UpdateCurrentQueueStateDTO;
import com.kyf.mp.server.modules.queue.entity.Queues;
import com.kyf.mp.server.modules.queue.vo.AddSongToQueueVO;
import com.kyf.mp.server.modules.queue.vo.AlterQueueVO;
import com.kyf.mp.server.modules.queue.vo.CreateQueueFromPlaylistVO;
import com.kyf.mp.server.modules.queue.vo.CurrentQueueVO;
import com.kyf.mp.server.modules.queue.vo.DeleteQueueVO;
import com.kyf.mp.server.modules.queue.vo.MyQueuesVO;
import com.kyf.mp.server.modules.queue.vo.SingleQueueVO;

/**
 * 队列数据访问层：复杂数据库操作在此定义。
 */
public interface QueuesBusiness extends BaseBusiness<Queues> {
    CurrentQueueVO getCurrentQueue(Long userId);

    MyQueuesVO getMyQueues(Long userId);

    SingleQueueVO getQueueById(Long userId, Long queueId);

    DeleteQueueVO deleteQueue(Long userId, Long queueId);

    void clearQueue(Long userId, Long queueId);

    CreateQueueFromPlaylistVO createQueueFromPlaylist(Long userId, Long playlistId);

    AddSongToQueueVO addSongToQueue(Long userId, Long paramQueueId, AddSongToQueueDTO dto);

    void removeSongFromQueue(Long userId, Long queueId, Long queueItemId);

    void updateCurrentQueueState(Long userId, UpdateCurrentQueueStateDTO wrapper);

    AlterQueueVO alterQueueToCurrent(Long userId, Long queueId);

    void setPlayMode(Long userId, Long queueId, String playMode);

    void reorderQueue(Long userId, Long queueId, List<Long> songIds);
}
