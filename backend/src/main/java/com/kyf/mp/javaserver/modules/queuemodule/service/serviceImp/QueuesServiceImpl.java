package com.kyf.mp.javaserver.modules.queuemodule.service.serviceImp;

import java.util.List;

import org.springframework.stereotype.Service;

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
    public CurrentQueue getCurrentQueue(Integer userId) {
        return queuesBusiness.getCurrentQueue(userId);
    }

    @Override
    public MyQueues getMyQueues(Integer userId) {
        return queuesBusiness.getMyQueues(userId);
    }

    @Override
    public SingleQueue getQueueById(Integer queueId) {
        return queuesBusiness.getQueueById(queueId);
    }

    @Override
    public DeleteQueueVO deleteQueue(Integer userId, Integer queueId) {
        return queuesBusiness.deleteQueue(userId, queueId);
    }

    @Override
    public void clearQueue(Integer userId, Integer queueId) {
        queuesBusiness.clearQueue(userId, queueId);
    }

    @Override
    public CreateQueueFromPlaylist createQueueFromPlaylist(Integer userId, Integer playlistId) {
        return queuesBusiness.createQueueFromPlaylist(userId, playlistId);
    }

    @Override
    public AddSongToQueueVO addSongToQueue(Integer userId, Integer paramQueueId, AddSongToQueue dto) {
        return queuesBusiness.addSongToQueue(userId, paramQueueId, dto);
    }

    @Override
    public void removeSongFromQueue(Integer userId, Integer queueItemId) {
        queuesBusiness.removeSongFromQueue(userId, queueItemId);
    }

    @Override
    public void updateCurrentQueueState(Integer userId, UpdateCurrentQueueStateDTO wrapper) {
        queuesBusiness.updateCurrentQueueState(userId, wrapper);
    }

    @Override
    public AlterQueueVO alterQueueToCurrent(Integer userId, Integer queueId) {
        return queuesBusiness.alterQueueToCurrent(userId, queueId);
    }

    @Override
    public void setPlayMode(Integer userId, Integer queueId, String playMode) {
        queuesBusiness.setPlayMode(userId, queueId, playMode);
    }

    @Override
    public void reorderQueue(Integer userId, Integer queueId, List<Integer> songIds) {
        queuesBusiness.reorderQueue(userId, queueId, songIds);
    }
}
