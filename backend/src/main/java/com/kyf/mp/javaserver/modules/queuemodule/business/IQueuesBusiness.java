package com.kyf.mp.javaserver.modules.queuemodule.business;

import java.util.List;

import com.kyf.mp.javaserver.common.business.IBaseBusiness;
import com.kyf.mp.javaserver.modules.queuemodule.dto.AddSongToQueue;
import com.kyf.mp.javaserver.modules.queuemodule.dto.UpdateCurrentQueueStateDTO;
import com.kyf.mp.javaserver.modules.queuemodule.entity.Queues;
import com.kyf.mp.javaserver.modules.queuemodule.vo.AddSongToQueueVO;
import com.kyf.mp.javaserver.modules.queuemodule.vo.AlterQueueVO;
import com.kyf.mp.javaserver.modules.queuemodule.vo.CreateQueueFromPlaylist;
import com.kyf.mp.javaserver.modules.queuemodule.vo.CurrentQueue;
import com.kyf.mp.javaserver.modules.queuemodule.vo.DeleteQueueVO;
import com.kyf.mp.javaserver.modules.queuemodule.vo.MyQueues;
import com.kyf.mp.javaserver.modules.queuemodule.vo.SingleQueue;

/**
 * 队列数据访问层：复杂数据库操作在此定义。
 */
public interface IQueuesBusiness extends IBaseBusiness<Queues> {
    CurrentQueue getCurrentQueue(Long userId);

    MyQueues getMyQueues(Long userId);

    SingleQueue getQueueById(Long queueId);

    DeleteQueueVO deleteQueue(Long userId, Long queueId);

    void clearQueue(Long userId, Long queueId);

    CreateQueueFromPlaylist createQueueFromPlaylist(Long userId, Long playlistId);

    AddSongToQueueVO addSongToQueue(Long userId, Long paramQueueId, AddSongToQueue dto);

    void removeSongFromQueue(Long userId, Long queueItemId);

    void updateCurrentQueueState(Long userId, UpdateCurrentQueueStateDTO wrapper);

    AlterQueueVO alterQueueToCurrent(Long userId, Long queueId);

    void setPlayMode(Long userId, Long queueId, String playMode);

    void reorderQueue(Long userId, Long queueId, List<Long> songIds);
}
