package com.kyf.mp.server.modules.queue.service.imp;

import java.util.List;

import org.springframework.stereotype.Service;

import com.kyf.mp.server.modules.queue.business.QueuesBusiness;
import com.kyf.mp.server.modules.queue.dto.AddSongToQueue;
import com.kyf.mp.server.modules.queue.dto.UpdateCurrentQueueStateDTO;
import com.kyf.mp.server.modules.queue.service.QueuesService;
import com.kyf.mp.server.modules.queue.vo.AddSongToQueueVO;
import com.kyf.mp.server.modules.queue.vo.AlterQueueVO;
import com.kyf.mp.server.modules.queue.vo.CreateQueueFromPlaylist;
import com.kyf.mp.server.modules.queue.vo.CurrentQueue;
import com.kyf.mp.server.modules.queue.vo.DeleteQueueVO;
import com.kyf.mp.server.modules.queue.vo.MyQueues;
import com.kyf.mp.server.modules.queue.vo.SingleQueue;

import lombok.RequiredArgsConstructor;

/**
 * <p>
 * 服务实现类：业务逻辑编排，数据访问委托给 QueuesBusiness。
 * </p>
 *
 * @author kyf
 * @since 2026-04-05
 */
@Service
@RequiredArgsConstructor
public class QueuesServiceImpl implements QueuesService {

    private final QueuesBusiness queuesBusiness;

    @Override
    public CurrentQueue getCurrentQueue(Long userId) {
        return queuesBusiness.getCurrentQueue(userId);
    }

    @Override
    public MyQueues getMyQueues(Long userId) {
        return queuesBusiness.getMyQueues(userId);
    }

    @Override
    public SingleQueue getQueueById(Long queueId) {
        return queuesBusiness.getQueueById(queueId);
    }

    @Override
    public DeleteQueueVO deleteQueue(Long userId, Long queueId) {
        return queuesBusiness.deleteQueue(userId, queueId);
    }

    @Override
    public void clearQueue(Long userId, Long queueId) {
        queuesBusiness.clearQueue(userId, queueId);
    }

    @Override
    public CreateQueueFromPlaylist createQueueFromPlaylist(Long userId, Long playlistId) {
        return queuesBusiness.createQueueFromPlaylist(userId, playlistId);
    }

    @Override
    public AddSongToQueueVO addSongToQueue(Long userId, Long paramQueueId, AddSongToQueue dto) {
        return queuesBusiness.addSongToQueue(userId, paramQueueId, dto);
    }

    @Override
    public void removeSongFromQueue(Long userId, Long queueItemId) {
        queuesBusiness.removeSongFromQueue(userId, queueItemId);
    }

    @Override
    public void updateCurrentQueueState(Long userId, UpdateCurrentQueueStateDTO wrapper) {
        queuesBusiness.updateCurrentQueueState(userId, wrapper);
    }

    @Override
    public AlterQueueVO alterQueueToCurrent(Long userId, Long queueId) {
        return queuesBusiness.alterQueueToCurrent(userId, queueId);
    }

    @Override
    public void setPlayMode(Long userId, Long queueId, String playMode) {
        queuesBusiness.setPlayMode(userId, queueId, playMode);
    }

    @Override
    public void reorderQueue(Long userId, Long queueId, List<Long> songIds) {
        queuesBusiness.reorderQueue(userId, queueId, songIds);
    }
}
