package com.kyf.mp.javaserver.modules.queuemodule.serviceImp;

import java.util.List;

import org.springframework.stereotype.Service;

import com.kyf.mp.javaserver.common.ResultModel;
import com.kyf.mp.javaserver.modules.queuemodule.business.IQueuesBusiness;
import com.kyf.mp.javaserver.modules.queuemodule.dto.AddSongToQueue;
import com.kyf.mp.javaserver.modules.queuemodule.dto.UpdateCurrentQueueStateDTO;
import com.kyf.mp.javaserver.modules.queuemodule.service.IQueuesService;
import com.kyf.mp.javaserver.modules.queuemodule.vo.AddSongToQueueVO;
import com.kyf.mp.javaserver.modules.queuemodule.vo.AlterQueueVO;
import com.kyf.mp.javaserver.modules.queuemodule.vo.CreateQueueFromPlaylist;
import com.kyf.mp.javaserver.modules.queuemodule.vo.CurrentQueue;
import com.kyf.mp.javaserver.modules.queuemodule.vo.DeleteQueueVO;
import com.kyf.mp.javaserver.modules.queuemodule.vo.MyQueues;
import com.kyf.mp.javaserver.modules.queuemodule.vo.SingleQueue;

import lombok.RequiredArgsConstructor;

/**
 * <p>
 * 服务实现类：业务逻辑编排，数据访问委托给 IQueuesBusiness。
 * </p>
 *
 * @author kyf
 * @since 2026-04-05
 */
@Service
@RequiredArgsConstructor
public class QueuesServiceImpl implements IQueuesService {

    private final IQueuesBusiness queuesBusiness;

    @Override
    public ResultModel<CurrentQueue> getCurrentQueue(Integer userId) {
        return queuesBusiness.getCurrentQueue(userId);
    }

    @Override
    public ResultModel<MyQueues> getMyQueues(Integer userId) {
        return queuesBusiness.getMyQueues(userId);
    }

    @Override
    public ResultModel<SingleQueue> getQueueById(Integer queueId) {
        return queuesBusiness.getQueueById(queueId);
    }

    @Override
    public ResultModel<DeleteQueueVO> deleteQueue(Integer userId, Integer queueId) {
        return queuesBusiness.deleteQueue(userId, queueId);
    }

    @Override
    public ResultModel<Void> clearQueue(Integer userId, Integer queueId) {
        return queuesBusiness.clearQueue(userId, queueId);
    }

    @Override
    public ResultModel<CreateQueueFromPlaylist> createQueueFromPlaylist(Integer userId, Integer playlistId) {
        return queuesBusiness.createQueueFromPlaylist(userId, playlistId);
    }

    @Override
    public ResultModel<AddSongToQueueVO> addSongToQueue(Integer userId, Integer paramQueueId, AddSongToQueue dto) {
        return queuesBusiness.addSongToQueue(userId, paramQueueId, dto);
    }

    @Override
    public ResultModel<Void> removeSongFromQueue(Integer userId, Integer queueItemId) {
        return queuesBusiness.removeSongFromQueue(userId, queueItemId);
    }

    @Override
    public ResultModel<Void> updateCurrentQueueState(Integer userId, UpdateCurrentQueueStateDTO wrapper) {
        return queuesBusiness.updateCurrentQueueState(userId, wrapper);
    }

    @Override
    public ResultModel<AlterQueueVO> alterQueueToCurrent(Integer userId, Integer queueId) {
        return queuesBusiness.alterQueueToCurrent(userId, queueId);
    }

    @Override
    public ResultModel<Void> setPlayMode(Integer userId, Integer queueId, String playMode) {
        return queuesBusiness.setPlayMode(userId, queueId, playMode);
    }

    @Override
    public ResultModel<Void> reorderQueue(Integer userId, Integer queueId, List<Integer> songIds) {
        return queuesBusiness.reorderQueue(userId, queueId, songIds);
    }
}
