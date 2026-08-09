package com.kyf.mp.javaserver.modules.queuemodule.service;

import com.kyf.mp.javaserver.common.ResultModel;
import com.kyf.mp.javaserver.modules.queuemodule.dto.AddSongToQueue;
import com.kyf.mp.javaserver.modules.queuemodule.dto.UpdateCurrentQueueStateDTO;
import com.kyf.mp.javaserver.modules.queuemodule.vo.AddSongToQueueVO;
import com.kyf.mp.javaserver.modules.queuemodule.vo.AlterQueueVO;
import com.kyf.mp.javaserver.modules.queuemodule.vo.CreateQueueFromPlaylist;
import com.kyf.mp.javaserver.modules.queuemodule.vo.CurrentQueue;
import com.kyf.mp.javaserver.modules.queuemodule.vo.DeleteQueueVO;
import com.kyf.mp.javaserver.modules.queuemodule.vo.MyQueues;
import com.kyf.mp.javaserver.modules.queuemodule.vo.SingleQueue;
import com.kyf.mp.javaserver.modules.queuemodule.entity.Queues;

import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author kyf
 * @since 2026-04-05
 */
@Service
public interface IQueuesService extends IService<Queues> {
    public ResultModel<CurrentQueue> getCurrentQueue(Integer userId);

    public ResultModel<MyQueues> getMyQueues(Integer userId);

    public ResultModel<SingleQueue> getQueueById(Integer queueId);

    public ResultModel<DeleteQueueVO> deleteQueue(Integer userId, Integer queueId);

    public ResultModel<Void> clearQueue(Integer userId, Integer queueId);

    public ResultModel<CreateQueueFromPlaylist> createQueueFromPlaylist(Integer userId, Integer playlistId);

    public ResultModel<AddSongToQueueVO> addSongToQueue(Integer userId, Integer paramQueueId, AddSongToQueue dto);

    public ResultModel<Void> removeSongFromQueue(Integer userId, Integer queueItemId);

    public ResultModel<Void> updateCurrentQueueState(Integer userId, UpdateCurrentQueueStateDTO wrapper);

    public ResultModel<AlterQueueVO> alterQueueToCurrent(Integer userId, Integer queueId);

    public ResultModel<Void> setPlayMode(Integer userId, Integer queueId, String playMode);

    public ResultModel<Void> reorderQueue(Integer userId, Integer queueId, List<Integer> songIds);
}
