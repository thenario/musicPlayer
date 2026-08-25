package com.kyf.mp.server.modules.queue.service;

import java.util.List;

import com.kyf.mp.server.modules.queue.dto.AddSongToQueueDTO;
import com.kyf.mp.server.modules.queue.dto.UpdateCurrentQueueStateDTO;
import com.kyf.mp.server.modules.queue.vo.AddSongToQueueVO;
import com.kyf.mp.server.modules.queue.vo.AlterQueueVO;
import com.kyf.mp.server.modules.queue.vo.CreateQueueFromPlaylistVO;
import com.kyf.mp.server.modules.queue.vo.CurrentQueueVO;
import com.kyf.mp.server.modules.queue.vo.DeleteQueueVO;
import com.kyf.mp.server.modules.queue.vo.MyQueuesVO;
import com.kyf.mp.server.modules.queue.vo.SingleQueueVO;

/**
 * <p>
 * 服务类：业务逻辑层，不再继承 IService（基础 CRUD 由 business 层提供）。
 * </p>
 *
 * @author kyf
 * @since 2026-04-05
 */
public interface QueuesService {
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
