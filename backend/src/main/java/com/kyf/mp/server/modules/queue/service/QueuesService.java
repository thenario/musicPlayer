package com.kyf.mp.server.modules.queue.service;

import java.util.List;

import com.kyf.mp.server.modules.queue.dto.AddSongToQueue;
import com.kyf.mp.server.modules.queue.dto.UpdateCurrentQueueStateDTO;
import com.kyf.mp.server.modules.queue.vo.AddSongToQueueVO;
import com.kyf.mp.server.modules.queue.vo.AlterQueueVO;
import com.kyf.mp.server.modules.queue.vo.CreateQueueFromPlaylist;
import com.kyf.mp.server.modules.queue.vo.CurrentQueue;
import com.kyf.mp.server.modules.queue.vo.DeleteQueueVO;
import com.kyf.mp.server.modules.queue.vo.MyQueues;
import com.kyf.mp.server.modules.queue.vo.SingleQueue;

/**
 * <p>
 * 服务类：业务逻辑层，不再继承 IService（基础 CRUD 由 business 层提供）。
 * </p>
 *
 * @author kyf
 * @since 2026-04-05
 */
public interface QueuesService {
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
