package com.kyf.mp.javaserver.modules.queuemodule.service;

import java.util.List;

import com.kyf.mp.javaserver.modules.queuemodule.dto.AddSongToQueue;
import com.kyf.mp.javaserver.modules.queuemodule.dto.UpdateCurrentQueueStateDTO;
import com.kyf.mp.javaserver.modules.queuemodule.vo.AddSongToQueueVO;
import com.kyf.mp.javaserver.modules.queuemodule.vo.AlterQueueVO;
import com.kyf.mp.javaserver.modules.queuemodule.vo.CreateQueueFromPlaylist;
import com.kyf.mp.javaserver.modules.queuemodule.vo.CurrentQueue;
import com.kyf.mp.javaserver.modules.queuemodule.vo.DeleteQueueVO;
import com.kyf.mp.javaserver.modules.queuemodule.vo.MyQueues;
import com.kyf.mp.javaserver.modules.queuemodule.vo.SingleQueue;

/**
 * <p>
 * 服务类：业务逻辑层，不再继承 IService（基础 CRUD 由 business 层提供）。
 * </p>
 *
 * @author kyf
 * @since 2026-04-05
 */
public interface IQueuesService {
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
