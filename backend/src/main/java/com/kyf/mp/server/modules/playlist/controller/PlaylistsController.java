package com.kyf.mp.server.modules.playlist.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.kyf.mp.server.common.ResultModel;
import com.kyf.mp.server.modules.playlist.vo.AddSongToPlaylistVO;
import com.kyf.mp.server.modules.playlist.vo.MyPlaylistsVO;
import com.kyf.mp.server.modules.playlist.vo.PlaylistActionVO;
import com.kyf.mp.server.modules.playlist.vo.PlaylistDetailVO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.kyf.mp.server.modules.playlist.service.PlaylistsService;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
@RequestMapping("/api/playlists")
@RequiredArgsConstructor
@Tag(name = "歌单", description = "歌单的创建、编辑和歌曲管理")
@SecurityRequirement(name = "bearerAuth")
public class PlaylistsController {
    private final PlaylistsService playlistsService;

    @PostMapping
    @Operation(summary = "创建歌单", description = "上传歌单封面并为当前用户创建歌单")
    public ResultModel<PlaylistActionVO> createPlaylist(
            @Parameter(description = "歌单封面图片", required = true)
            @RequestParam("cover_image") @NotNull(message = "请上传歌单封面") MultipartFile file,
            @Parameter(description = "歌单名称", required = true, example = "我的收藏")
            @RequestParam("name") @NotBlank(message = "歌单名称不能为空") String name,
            @Parameter(description = "歌单描述", example = "平时喜欢听的歌曲")
            @RequestParam(value = "description", required = false) String description,
            @Parameter(hidden = true) @RequestAttribute(value = "userId", required = false) @NotNull(message = "用户ID不能为空") Long userId) {
        return ResultModel.success(playlistsService.createPlaylist(file, name, description, userId));
    }

    @PatchMapping
    @Operation(summary = "编辑歌单信息", description = "只能编辑当前用户创建的歌单；所有修改字段均为可选")
    public ResultModel<PlaylistActionVO> editPlaylist(
            @Parameter(description = "新的歌单封面图片")
            @RequestParam(value = "cover_image", required = false) MultipartFile file,
            @Parameter(description = "歌单 ID", required = true, example = "1")
            @RequestParam("playlist_id") @NotNull(message = "歌单ID不能为空") @Min(value = 1, message = "ID must be positive") Long playlistId,
            @Parameter(description = "新的歌单名称", example = "我的收藏")
            @RequestParam(value = "name", required = false) @NotBlank(message = "歌单名称不能为空") String name,
            @Parameter(description = "新的歌单描述")
            @RequestParam(value = "description", required = false) String description,
            @Parameter(hidden = true) @RequestAttribute(value = "userId", required = false) @NotNull(message = "用户ID不能为空") Long userId) {
        return ResultModel.success(playlistsService.editPlaylist(file, playlistId, name, description, userId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除歌单", description = "只能删除当前用户创建的歌单")
    public ResultModel<Void> deletePlaylist(
            @Parameter(description = "歌单 ID", required = true, example = "1")
            @PathVariable("id") @NotNull(message = "歌单ID不能为空") @Min(value = 1, message = "ID must be positive") Long id,
            @Parameter(hidden = true) @RequestAttribute(value = "userId", required = false) @NotNull(message = "用户ID不能为空") Long userId) {
        playlistsService.deletePlaylist(id, userId);
        return ResultModel.success(null);
    }

    @GetMapping
    @Operation(summary = "获取我的歌单列表", description = "返回当前用户创建的全部歌单摘要")
    public ResultModel<MyPlaylistsVO> getMyPlaylists(
            @Parameter(hidden = true) @RequestAttribute(value = "userId", required = false) @NotNull(message = "用户ID不能为空") Long userId) {
        return ResultModel.success(playlistsService.getMyPlaylists(userId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取歌单详情", description = "返回歌单基本信息、歌曲列表和当前用户的点赞状态")
    public ResultModel<PlaylistDetailVO> getPlaylistById(
            @Parameter(description = "歌单 ID", required = true, example = "1")
            @PathVariable("id") @NotNull(message = "歌单ID不能为空") @Min(value = 1, message = "ID must be positive") Long id,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId) {
        return ResultModel.success(playlistsService.getPlaylistDetail(id, userId));
    }

    @PostMapping("/{id}/likes")
    @Operation(summary = "点赞歌单")
    public ResultModel<Void> like(
            @Parameter(description = "歌单 ID", required = true, example = "1")
            @PathVariable("id") @NotNull(message = "参数不完整") @Min(value = 1, message = "ID must be positive") Long id,
            @Parameter(hidden = true) @RequestAttribute(value = "userId", required = false) @NotNull(message = "参数不完整") Long userId) {
        playlistsService.toggleLike(id, userId, true);
        return ResultModel.success(null);
    }

    @DeleteMapping("/{id}/unlikes")
    @Operation(summary = "取消点赞歌单")
    public ResultModel<Void> unlike(
            @Parameter(description = "歌单 ID", required = true, example = "1")
            @PathVariable("id") @NotNull(message = "参数不完整") @Min(value = 1, message = "ID must be positive") Long id,
            @Parameter(hidden = true) @RequestAttribute(value = "userId", required = false) @NotNull(message = "参数不完整") Long userId) {
        playlistsService.toggleLike(id, userId, false);
        return ResultModel.success(null);
    }

    @PostMapping("/{playlist_id}/songs/{song_id}")
    @Operation(summary = "向歌单添加歌曲", description = "只能向当前用户创建的歌单添加歌曲")
    public ResultModel<AddSongToPlaylistVO> addSong(
            @Parameter(description = "歌单 ID", required = true, example = "1")
            @PathVariable("playlist_id") @NotNull(message = "歌单ID或歌曲ID不能为空") @Min(value = 1, message = "ID must be positive") Long playlistId,
            @Parameter(description = "歌曲 ID", required = true, example = "1")
            @PathVariable("song_id") @NotNull(message = "歌单ID或歌曲ID不能为空") @Min(value = 1, message = "ID must be positive") Long songId,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId) {
        return ResultModel.success(playlistsService.addSongToPlaylist(playlistId, songId, userId));
    }

    @DeleteMapping("/{playlist_id}/songs/{song_id}")
    @Operation(summary = "从歌单移除歌曲", description = "只能从当前用户创建的歌单移除歌曲")
    public ResultModel<Void> removeSong(
            @Parameter(description = "歌单 ID", required = true, example = "1")
            @PathVariable("playlist_id") @NotNull(message = "歌单ID或歌曲ID不能为空") @Min(value = 1, message = "ID must be positive") Long playlistId,
            @Parameter(description = "歌曲 ID", required = true, example = "1")
            @PathVariable("song_id") @NotNull(message = "歌单ID或歌曲ID不能为空") @Min(value = 1, message = "ID must be positive") Long songId,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId) {
        playlistsService.removeSongFromPlaylist(playlistId, songId, userId);
        return ResultModel.success(null);
    }
}
