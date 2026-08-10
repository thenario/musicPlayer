package com.kyf.mp.javaserver.modules.queuemodule.business;

import java.util.List;

import com.kyf.mp.javaserver.common.ResultModel;
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
    ResultModel<CurrentQueue> getCurrentQueue(Integer userId);

    ResultModel<MyQueues> getMyQueues(Integer userId);

    ResultModel<SingleQueue> getQueueById(Integer queueId);

    ResultModel<DeleteQueueVO> deleteQueue(Integer userId, Integer queueId);

    ResultModel<Void> clearQueue(Integer userId, Integer queueId);

    ResultModel<CreateQueueFromPlaylist> createQueueFromPlaylist(Integer userId, Integer playlistId);

    ResultModel<AddSongToQueueVO> addSongToQueue(Integer userId, Integer paramQueueId, AddSongToQueue dto);

    ResultModel<Void> removeSongFromQueue(Integer userId, Integer queueItemId);

    ResultModel<Void> updateCurrentQueueState(Integer userId, UpdateCurrentQueueStateDTO wrapper);

    ResultModel<AlterQueueVO> alterQueueToCurrent(Integer userId, Integer queueId);

    ResultModel<Void> setPlayMode(Integer userId, Integer queueId, String playMode);

    ResultModel<Void> reorderQueue(Integer userId, Integer queueId, List<Integer> songIds);
}
