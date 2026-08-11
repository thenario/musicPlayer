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
    CurrentQueue getCurrentQueue(Integer userId);

    MyQueues getMyQueues(Integer userId);

    SingleQueue getQueueById(Integer queueId);

    DeleteQueueVO deleteQueue(Integer userId, Integer queueId);

    void clearQueue(Integer userId, Integer queueId);

    CreateQueueFromPlaylist createQueueFromPlaylist(Integer userId, Integer playlistId);

    AddSongToQueueVO addSongToQueue(Integer userId, Integer paramQueueId, AddSongToQueue dto);

    void removeSongFromQueue(Integer userId, Integer queueItemId);

    void updateCurrentQueueState(Integer userId, UpdateCurrentQueueStateDTO wrapper);

    AlterQueueVO alterQueueToCurrent(Integer userId, Integer queueId);

    void setPlayMode(Integer userId, Integer queueId, String playMode);

    void reorderQueue(Integer userId, Integer queueId, List<Integer> songIds);
}
