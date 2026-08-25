package com.kyf.mp.server.modules.queue.controller;

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

import com.kyf.mp.server.common.ResultModel;
import com.kyf.mp.server.modules.queue.dto.AddSongToQueueDTO;
import com.kyf.mp.server.modules.queue.dto.AlterQueueDTO;
import com.kyf.mp.server.modules.queue.dto.CreateQueueFromPlaylistDTO;
import com.kyf.mp.server.modules.queue.dto.ReorderDTO;
import com.kyf.mp.server.modules.queue.dto.SetPlayModeDTO;
import com.kyf.mp.server.modules.queue.dto.UpdateQueueStateDataDTO;
import com.kyf.mp.server.modules.queue.vo.AddSongToQueueVO;
import com.kyf.mp.server.modules.queue.vo.AlterQueueVO;
import com.kyf.mp.server.modules.queue.vo.CreateQueueFromPlaylistVO;
import com.kyf.mp.server.modules.queue.vo.CurrentQueueVO;
import com.kyf.mp.server.modules.queue.vo.DeleteQueueVO;
import com.kyf.mp.server.modules.queue.vo.MyQueuesVO;
import com.kyf.mp.server.modules.queue.vo.SingleQueueVO;
import com.kyf.mp.server.modules.queue.service.QueuesService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

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
@Tag(name = "播放队列", description = "播放队列、当前播放状态和队列歌曲管理")
@SecurityRequirement(name = "bearerAuth")
public class QueuesController {
    private final QueuesService queueService;

    @GetMapping("/current")
    @Operation(summary = "获取当前播放队列", description = "返回当前用户的播放状态和当前队列详情")
    public ResultModel<CurrentQueueVO> getCurrentQueue(
            @Parameter(hidden = true)
            @RequestAttribute(value = "userId", required = false) @NotNull(message = "用户未登录或非法请求") @Min(value = 1, message = "用户未登录或非法请求") Long userId) {
        return ResultModel.success(queueService.getCurrentQueue(userId));
    }

    @PatchMapping("/current/state")
    @Operation(summary = "更新当前播放状态", description = "更新当前队列、歌曲、进度和播放模式")
    public ResultModel<Void> updateCurrentQueueState(
            @Parameter(hidden = true)
            @RequestAttribute(value = "userId", required = false) @NotNull(message = "用户未登录") @Min(value = 1, message = "用户未登录") Long userId,
            @RequestBody @Valid @NotNull(message = "缺少播放状态数据") UpdateQueueStateDataDTO wrapper) {
        queueService.updateCurrentQueueState(userId, wrapper.getStateData());
        return ResultModel.success(null);
    }

    @PutMapping("/player/current-queue")
    @Operation(summary = "切换当前播放队列", description = "将指定队列设为当前播放队列，并从第一首歌开始播放")
    public ResultModel<AlterQueueVO> alterQueueToCurrent(
            @Parameter(hidden = true)
            @RequestAttribute(value = "userId", required = false) @NotNull(message = "用户未登录") @Min(value = 1, message = "用户未登录") Long userId,
            @RequestBody @Valid @NotNull(message = "缺少 queue_id") AlterQueueDTO dto) {
        return ResultModel.success(queueService.alterQueueToCurrent(userId, dto.getQueueId()));
    }

    @GetMapping
    @Operation(summary = "获取我的队列列表", description = "返回当前用户创建的全部播放队列")
    public ResultModel<MyQueuesVO> getMyQueues(
            @Parameter(hidden = true)
            @RequestAttribute(value = "userId", required = false) @NotNull(message = "用户未登录或非法请求") @Min(value = 1, message = "用户未登录或非法请求") Long userId) {
        return ResultModel.success(queueService.getMyQueues(userId));
    }

    @PostMapping
    @Operation(summary = "从歌单创建播放队列", description = "根据指定歌单创建当前用户的播放队列")
    public ResultModel<CreateQueueFromPlaylistVO> createQueueFromPlaylist(
            @Parameter(hidden = true)
            @RequestAttribute(value = "userId", required = false) @NotNull(message = "用户未登录或非法请求") @Min(value = 1, message = "用户未登录或非法请求") Long userId,
            @RequestBody @Valid @NotNull(message = "歌单ID不能为空") CreateQueueFromPlaylistDTO dto) {
        return ResultModel.success(queueService.createQueueFromPlaylist(userId, dto.getPlaylistId()));
    }

    @GetMapping("/{queueId}")
    @Operation(summary = "获取队列详情", description = "返回指定队列及其歌曲列表")
    public ResultModel<SingleQueueVO> getQueueById(
            @Parameter(hidden = true)
            @RequestAttribute(value = "userId", required = false) @NotNull(message = "用户未登录") @Min(value = 1, message = "用户未登录") Long userId,
            @Parameter(description = "队列 ID", required = true, example = "1")
            @PathVariable @NotNull(message = "无效的队列ID") @Min(value = 1, message = "无效的队列ID") Long queueId) {
        return ResultModel.success(queueService.getQueueById(userId, queueId));
    }

    @DeleteMapping("/{queueId}")
    @Operation(summary = "删除播放队列", description = "只能删除当前用户创建的队列")
    public ResultModel<DeleteQueueVO> deleteQueue(
            @Parameter(hidden = true)
            @RequestAttribute(value = "userId", required = false) @NotNull(message = "用户未登录或非法请求") @Min(value = 1, message = "用户未登录或非法请求") Long userId,
            @Parameter(description = "队列 ID", required = true, example = "1")
            @PathVariable @NotNull(message = "无效的队列ID") @Min(value = 1, message = "无效的队列ID") Long queueId) {
        return ResultModel.success(queueService.deleteQueue(userId, queueId));
    }

    @DeleteMapping("/{queueId}/songs")
    @Operation(summary = "清空队列歌曲", description = "移除指定队列中的全部歌曲")
    public ResultModel<Void> clearQueue(
            @Parameter(hidden = true)
            @RequestAttribute(value = "userId", required = false) @NotNull(message = "用户未登录或非法请求") @Min(value = 1, message = "用户未登录或非法请求") Long userId,
            @Parameter(description = "队列 ID", required = true, example = "1")
            @PathVariable @NotNull(message = "无效的队列ID") @Min(value = 1, message = "无效的队列ID") Long queueId) {
        queueService.clearQueue(userId, queueId);
        return ResultModel.success(null);
    }

    @PostMapping("/{queueId}/songs")
    @Operation(summary = "向队列添加歌曲", description = "向指定队列添加一首歌曲")
    public ResultModel<AddSongToQueueVO> addSongToQueue(
            @Parameter(hidden = true)
            @RequestAttribute(value = "userId", required = false) @NotNull(message = "用户未登录") @Min(value = 1, message = "用户未登录") Long userId,
            @Parameter(description = "队列 ID", required = true, example = "1")
            @PathVariable Long queueId,
            @RequestBody @Valid @NotNull(message = "请求体不能为空") AddSongToQueueDTO dto) {
        Long finalParamId = (queueId == null || queueId <= 0) ? null : queueId;
        return ResultModel.success(queueService.addSongToQueue(userId, finalParamId, dto));
    }

    @DeleteMapping("/{queueId}/songs/{queueItemId}")
    @Operation(summary = "从队列移除歌曲", description = "根据队列项 ID 从指定队列移除歌曲")
    public ResultModel<Void> removeSongFromQueue(
            @Parameter(hidden = true)
            @RequestAttribute(value = "userId", required = false) @NotNull(message = "用户未登录") @Min(value = 1, message = "用户未登录") Long userId,
            @Parameter(description = "队列 ID", required = true, example = "1")
            @PathVariable @NotNull(message = "缺少有效的队列ID") @Min(value = 1, message = "缺少有效的队列ID") Long queueId,
            @Parameter(description = "队列项 ID", required = true, example = "1")
            @PathVariable @NotNull(message = "缺少有效的队列项ID") @Min(value = 1, message = "缺少有效的队列项ID") Long queueItemId) {
        queueService.removeSongFromQueue(userId, queueId, queueItemId);
        return ResultModel.success(null);
    }

    @PatchMapping("/{queueId}/play-mode")
    @Operation(summary = "设置播放模式", description = "设置指定队列的顺序、随机或循环播放模式")
    public ResultModel<Void> setPlayMode(
            @Parameter(hidden = true)
            @RequestAttribute(value = "userId", required = false) @NotNull(message = "用户未登录") @Min(value = 1, message = "用户未登录") Long userId,
            @Parameter(description = "队列 ID", required = true, example = "1")
            @PathVariable @NotNull(message = "缺少必要参数") @Min(value = 1, message = "缺少必要参数") Long queueId,
            @RequestBody @Valid @NotNull(message = "缺少必要参数") SetPlayModeDTO dto) {
        queueService.setPlayMode(userId, queueId, dto.getPlayMode());
        return ResultModel.success(null);
    }

    @PatchMapping("/{queueId}/reorder")
    @Operation(summary = "调整队列歌曲顺序", description = "按照请求体中的歌曲 ID 顺序重新排列队列")
    public ResultModel<Void> reorderQueue(
            @Parameter(hidden = true)
            @RequestAttribute(value = "userId", required = false) @NotNull(message = "用户未登录") @Min(value = 1, message = "用户未登录") Long userId,
            @Parameter(description = "队列 ID", required = true, example = "1")
            @PathVariable @NotNull(message = "缺少队列ID") @Min(value = 1, message = "缺少队列ID") Long queueId,
            @RequestBody @Valid @NotNull(message = "无效的歌曲列表") ReorderDTO dto) {
        queueService.reorderQueue(userId, queueId, dto.getSongIds());
        return ResultModel.success(null);
    }
}
