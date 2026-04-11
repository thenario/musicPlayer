package com.kyf.mp.javaserver.modules.QueueModule.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kyf.mp.javaserver.common.BusinessException;
import com.kyf.mp.javaserver.common.ResultModel;
import com.kyf.mp.javaserver.modules.QueueModule.DTO.AddSongToQueue;
import com.kyf.mp.javaserver.modules.QueueModule.DTO.AlterQueueDTO;
import com.kyf.mp.javaserver.modules.QueueModule.DTO.CreateQueueFromPlaylistDTO;
import com.kyf.mp.javaserver.modules.QueueModule.DTO.ReorderDTO;
import com.kyf.mp.javaserver.modules.QueueModule.DTO.SetPlayModeDTO;
import com.kyf.mp.javaserver.modules.QueueModule.DTO.UpdateCurrentQueueStateDTO;
import com.kyf.mp.javaserver.modules.QueueModule.DTO.UpdateQueueStateDataDTO;
import com.kyf.mp.javaserver.modules.QueueModule.VO.AddSongToQueueVO;
import com.kyf.mp.javaserver.modules.QueueModule.VO.AlterQueueVO;
import com.kyf.mp.javaserver.modules.QueueModule.VO.CreateQueueFromPlaylist;
import com.kyf.mp.javaserver.modules.QueueModule.VO.CurrentQueue;
import com.kyf.mp.javaserver.modules.QueueModule.VO.DeleteQueueVO;
import com.kyf.mp.javaserver.modules.QueueModule.VO.MyQueues;
import com.kyf.mp.javaserver.modules.QueueModule.VO.SingleQueue;
import com.kyf.mp.javaserver.modules.QueueModule.service.IQueuesService;

import lombok.RequiredArgsConstructor;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author kyf
 * @since 2026-04-05
 */
@RestController
@RequestMapping("/api/queues")
@RequiredArgsConstructor
public class QueuesController {
    private final IQueuesService queueService;

    @GetMapping("/current")
    public ResultModel<CurrentQueue> getCurrentQueue(
            @RequestAttribute(value = "userId", required = true) Integer userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(401, "用户未登录或非法请求");
        }
        return queueService.getCurrentQueue(userId);
    }

    @PatchMapping("/current/state")
    public ResultModel<Void> updateCurrentQueueState(
            @RequestAttribute("userId") Integer userId,
            @RequestBody UpdateQueueStateDataDTO wrapper) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(401, "用户未登录");
        }
        if (wrapper == null || wrapper.getStateData() == null) {
            throw new BusinessException(400, "缺少播放状态数据");
        }
        UpdateCurrentQueueStateDTO dto = wrapper.getStateData();
        if (dto.getCurrentQueueId() == null) {
            throw new BusinessException(400, "缺少队列ID");
        }
        return queueService.updateCurrentQueueState(userId, dto);
    }

    @PutMapping("/player/current-queue")
    public ResultModel<AlterQueueVO> alterQueueToCurrent(
            @RequestAttribute("userId") Integer userId,
            @RequestBody AlterQueueDTO dto) {

        if (userId == null || userId <= 0) {
            throw new BusinessException(401, "用户未登录");
        }

        if (dto == null || dto.getQueueId() == null) {
            throw new BusinessException(400, "缺少 queue_id");
        }

        return queueService.alterQueueToCurrent(userId, dto.getQueueId());
    }

    @GetMapping
    public ResultModel<MyQueues> getMyQueues(@RequestAttribute("userId") Integer userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(401, "用户未登录或非法请求");
        }
        return queueService.getMyQueues(userId);
    }

    @PostMapping
    public ResultModel<CreateQueueFromPlaylist> createQueueFromPlaylist(
            @RequestAttribute("userId") Integer userId,
            @RequestBody CreateQueueFromPlaylistDTO dto) {

        if (userId == null || userId <= 0) {
            throw new BusinessException(401, "用户未登录或非法请求");
        }

        if (dto == null || dto.getPlaylistId() == null || dto.getPlaylistId() <= 0) {
            throw new BusinessException(400, "歌单ID不能为空且必须合法");
        }

        return queueService.createQueueFromPlaylist(userId, dto.getPlaylistId());
    }

    @GetMapping("/{queueId}")
    public ResultModel<SingleQueue> getQueueById(@PathVariable Integer queueId) {
        return queueService.getQueueById(queueId);
    }

    @DeleteMapping("/{queueId}")
    public ResultModel<DeleteQueueVO> deleteQueue(@RequestAttribute("userId") Integer userId,
            @PathVariable Integer queueId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(401, "用户未登录或非法请求");
        }
        return queueService.deleteQueue(userId, queueId);
    }

    @DeleteMapping("/{queueId}/songs")
    public ResultModel<Void> clearQueue(
            @RequestAttribute("userId") Integer userId,
            @PathVariable Integer queueId) {

        if (userId == null || userId <= 0) {
            throw new BusinessException(401, "用户未登录或非法请求");
        }

        if (queueId == null || queueId <= 0) {
            throw new BusinessException(400, "无效的队列ID");
        }

        return queueService.clearQueue(userId, queueId);
    }

    @PostMapping("/{queueId}/songs")
    public ResultModel<AddSongToQueueVO> addSongToQueue(
            @RequestAttribute("userId") Integer userId,
            @PathVariable Integer queueId,
            @RequestBody AddSongToQueue dto) {

        if (userId == null || userId <= 0) {
            throw new BusinessException(401, "用户未登录");
        }
        if (dto == null) {
            throw new BusinessException(400, "请求体不能为空");
        }

        if (dto.getSongId() == null || dto.getSongId() <= 0) {
            throw new BusinessException(400, "歌曲ID不能为空且必须合法");
        }

        Integer finalParamId = (queueId == null || queueId <= 0) ? null : queueId;

        return queueService.addSongToQueue(userId, finalParamId, dto);
    }

    @DeleteMapping("/{queueId}/songs/{queueItemId}")
    public ResultModel<Void> removeSongFromQueue(
            @RequestAttribute("userId") Integer userId,
            @PathVariable Integer queueId,
            @PathVariable Integer queueItemId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(401, "用户未登录");
        }
        if (queueItemId == null || queueItemId <= 0) {
            throw new BusinessException(400, "缺少有效的队列项ID");
        }
        return queueService.removeSongFromQueue(userId, queueItemId);
    }

    @PatchMapping("/{queueId}/play-mode")
    public ResultModel<Void> setPlayMode(
            @RequestAttribute("userId") Integer userId,
            @PathVariable Integer queueId,
            @RequestBody SetPlayModeDTO dto) {

        if (userId == null || userId <= 0) {
            throw new BusinessException(401, "用户未登录");
        }
        if (queueId == null || dto == null || dto.getPlayMode() == null) {
            throw new BusinessException(400, "缺少必要参数");
        }
        return queueService.setPlayMode(userId, queueId, dto.getPlayMode());
    }

    @PatchMapping("/{queueId}/reorder")
    public ResultModel<Void> reorderQueue(
            @RequestAttribute("userId") Integer userId,
            @PathVariable Integer queueId,
            @RequestBody ReorderDTO dto) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(401, "用户未登录");
        }
        if (queueId == null) {
            throw new BusinessException(400, "缺少队列ID");
        }
        if (dto == null || dto.getSongIds() == null || dto.getSongIds().isEmpty()) {
            throw new BusinessException(400, "无效的歌曲列表");
        }
        return queueService.reorderQueue(userId, queueId, dto.getSongIds());
    }
}
