package com.kyf.mp.javaserver.modules.queuemodule.controller;

import org.springframework.validation.annotation.Validated;
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

import com.kyf.mp.javaserver.common.ResultModel;
import com.kyf.mp.javaserver.modules.queuemodule.dto.AddSongToQueue;
import com.kyf.mp.javaserver.modules.queuemodule.dto.AlterQueueDTO;
import com.kyf.mp.javaserver.modules.queuemodule.dto.CreateQueueFromPlaylistDTO;
import com.kyf.mp.javaserver.modules.queuemodule.dto.ReorderDTO;
import com.kyf.mp.javaserver.modules.queuemodule.dto.SetPlayModeDTO;
import com.kyf.mp.javaserver.modules.queuemodule.dto.UpdateQueueStateDataDTO;
import com.kyf.mp.javaserver.modules.queuemodule.vo.AddSongToQueueVO;
import com.kyf.mp.javaserver.modules.queuemodule.vo.AlterQueueVO;
import com.kyf.mp.javaserver.modules.queuemodule.vo.CreateQueueFromPlaylist;
import com.kyf.mp.javaserver.modules.queuemodule.vo.CurrentQueue;
import com.kyf.mp.javaserver.modules.queuemodule.vo.DeleteQueueVO;
import com.kyf.mp.javaserver.modules.queuemodule.vo.MyQueues;
import com.kyf.mp.javaserver.modules.queuemodule.vo.SingleQueue;
import com.kyf.mp.javaserver.modules.queuemodule.service.IQueuesService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author kyf
 * @since 2026-04-05
 */
@Validated
@RestController
@RequestMapping("/api/queues")
@RequiredArgsConstructor
public class QueuesController {
    private final IQueuesService queueService;

    @GetMapping("/current")
    public ResultModel<CurrentQueue> getCurrentQueue(
            @RequestAttribute(value = "userId", required = false) @NotNull(message = "用户未登录或非法请求") @Min(value = 1, message = "用户未登录或非法请求") Long userId) {
        return ResultModel.success(queueService.getCurrentQueue(userId));
    }

    @PatchMapping("/current/state")
    public ResultModel<Void> updateCurrentQueueState(
            @RequestAttribute(value = "userId", required = false) @NotNull(message = "用户未登录") @Min(value = 1, message = "用户未登录") Long userId,
            @RequestBody @Valid @NotNull(message = "缺少播放状态数据") UpdateQueueStateDataDTO wrapper) {
        queueService.updateCurrentQueueState(userId, wrapper.getStateData());
        return ResultModel.success(null);
    }

    @PutMapping("/player/current-queue")
    public ResultModel<AlterQueueVO> alterQueueToCurrent(
            @RequestAttribute(value = "userId", required = false) @NotNull(message = "用户未登录") @Min(value = 1, message = "用户未登录") Long userId,
            @RequestBody @Valid @NotNull(message = "缺少 queue_id") AlterQueueDTO dto) {
        return ResultModel.success(queueService.alterQueueToCurrent(userId, dto.getQueueId()));
    }

    @GetMapping
    public ResultModel<MyQueues> getMyQueues(
            @RequestAttribute(value = "userId", required = false) @NotNull(message = "用户未登录或非法请求") @Min(value = 1, message = "用户未登录或非法请求") Long userId) {
        return ResultModel.success(queueService.getMyQueues(userId));
    }

    @PostMapping
    public ResultModel<CreateQueueFromPlaylist> createQueueFromPlaylist(
            @RequestAttribute(value = "userId", required = false) @NotNull(message = "用户未登录或非法请求") @Min(value = 1, message = "用户未登录或非法请求") Long userId,
            @RequestBody @Valid @NotNull(message = "歌单ID不能为空") CreateQueueFromPlaylistDTO dto) {
        return ResultModel.success(queueService.createQueueFromPlaylist(userId, dto.getPlaylistId()));
    }

    @GetMapping("/{queueId}")
    public ResultModel<SingleQueue> getQueueById(
            @PathVariable @NotNull(message = "无效的队列ID") @Min(value = 1, message = "无效的队列ID") Long queueId) {
        return ResultModel.success(queueService.getQueueById(queueId));
    }

    @DeleteMapping("/{queueId}")
    public ResultModel<DeleteQueueVO> deleteQueue(
            @RequestAttribute(value = "userId", required = false) @NotNull(message = "用户未登录或非法请求") @Min(value = 1, message = "用户未登录或非法请求") Long userId,
            @PathVariable @NotNull(message = "无效的队列ID") @Min(value = 1, message = "无效的队列ID") Long queueId) {
        return ResultModel.success(queueService.deleteQueue(userId, queueId));
    }

    @DeleteMapping("/{queueId}/songs")
    public ResultModel<Void> clearQueue(
            @RequestAttribute(value = "userId", required = false) @NotNull(message = "用户未登录或非法请求") @Min(value = 1, message = "用户未登录或非法请求") Long userId,
            @PathVariable @NotNull(message = "无效的队列ID") @Min(value = 1, message = "无效的队列ID") Long queueId) {
        queueService.clearQueue(userId, queueId);
        return ResultModel.success(null);
    }

    @PostMapping("/{queueId}/songs")
    public ResultModel<AddSongToQueueVO> addSongToQueue(
            @RequestAttribute(value = "userId", required = false) @NotNull(message = "用户未登录") @Min(value = 1, message = "用户未登录") Long userId,
            @PathVariable Long queueId,
            @RequestBody @Valid @NotNull(message = "请求体不能为空") AddSongToQueue dto) {
        Long finalParamId = (queueId == null || queueId <= 0) ? null : queueId;
        return ResultModel.success(queueService.addSongToQueue(userId, finalParamId, dto));
    }

    @DeleteMapping("/{queueId}/songs/{queueItemId}")
    public ResultModel<Void> removeSongFromQueue(
            @RequestAttribute(value = "userId", required = false) @NotNull(message = "用户未登录") @Min(value = 1, message = "用户未登录") Long userId,
            @PathVariable Long queueId,
            @PathVariable @NotNull(message = "缺少有效的队列项ID") @Min(value = 1, message = "缺少有效的队列项ID") Long queueItemId) {
        queueService.removeSongFromQueue(userId, queueItemId);
        return ResultModel.success(null);
    }

    @PatchMapping("/{queueId}/play-mode")
    public ResultModel<Void> setPlayMode(
            @RequestAttribute(value = "userId", required = false) @NotNull(message = "用户未登录") @Min(value = 1, message = "用户未登录") Long userId,
            @PathVariable @NotNull(message = "缺少必要参数") @Min(value = 1, message = "缺少必要参数") Long queueId,
            @RequestBody @Valid @NotNull(message = "缺少必要参数") SetPlayModeDTO dto) {
        queueService.setPlayMode(userId, queueId, dto.getPlayMode());
        return ResultModel.success(null);
    }

    @PatchMapping("/{queueId}/reorder")
    public ResultModel<Void> reorderQueue(
            @RequestAttribute(value = "userId", required = false) @NotNull(message = "用户未登录") @Min(value = 1, message = "用户未登录") Long userId,
            @PathVariable @NotNull(message = "缺少队列ID") @Min(value = 1, message = "缺少队列ID") Long queueId,
            @RequestBody @Valid @NotNull(message = "无效的歌曲列表") ReorderDTO dto) {
        queueService.reorderQueue(userId, queueId, dto.getSongIds());
        return ResultModel.success(null);
    }
}
