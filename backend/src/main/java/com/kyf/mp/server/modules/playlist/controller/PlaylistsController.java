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
import com.kyf.mp.server.modules.playlist.service.PlaylistsService;

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
public class PlaylistsController {
    private final PlaylistsService playlistsService;

    // 1. 创建歌单
    @PostMapping
    public ResultModel<PlaylistActionVO> createPlaylist(
            @RequestParam("cover_image") @NotNull(message = "请上传歌单封面") MultipartFile file,
            @RequestParam("name") @NotBlank(message = "歌单名称不能为空") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestAttribute(value = "userId", required = false) @NotNull(message = "用户ID不能为空") Long userId) {
        return ResultModel.success(playlistsService.createPlaylist(file, name, description, userId));
    }

    // 2. 编辑歌单详情
    @PatchMapping
    public ResultModel<PlaylistActionVO> editPlaylist(
            @RequestParam(value = "cover_image", required = false) MultipartFile file,
            @RequestParam("playlist_id") @NotNull(message = "歌单ID不能为空") Long playlistId,
            @RequestParam(value = "name", required = false) @NotBlank(message = "歌单名称不能为空") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestAttribute(value = "userId", required = false) @NotNull(message = "用户ID不能为空") Long userId) {
        return ResultModel.success(playlistsService.editPlaylist(file, playlistId, name, description, userId));
    }

    // 3. 删除歌单
    @DeleteMapping("/{id}")
    public ResultModel<Void> deletePlaylist(
            @PathVariable("id") @NotNull(message = "歌单ID不能为空") Long id,
            @RequestAttribute(value = "userId", required = false) @NotNull(message = "用户ID不能为空") Long userId) {
        playlistsService.deletePlaylist(id, userId);
        return ResultModel.success(null);
    }

    // 4. 获取我的歌单列表
    @GetMapping
    public ResultModel<MyPlaylistsVO> getMyPlaylists(
            @RequestAttribute(value = "userId", required = false) @NotNull(message = "用户ID不能为空") Long userId) {
        return ResultModel.success(playlistsService.getMyPlaylists(userId));
    }

    // 5. 获取歌单详情（含歌曲）
    @GetMapping("/{id}")
    public ResultModel<PlaylistDetailVO> getPlaylistById(
            @PathVariable("id") @NotNull(message = "歌单ID不能为空") Long id,
            @RequestAttribute("userId") Long userId) {
        return ResultModel.success(playlistsService.getPlaylistDetail(id, userId));
    }

    // 6. 点赞/取消点赞
    @PostMapping("/{id}/likes")
    public ResultModel<Void> like(
            @PathVariable("id") @NotNull(message = "参数不完整") Long id,
            @RequestAttribute(value = "userId", required = false) @NotNull(message = "参数不完整") Long userId) {
        playlistsService.toggleLike(id, userId, true);
        return ResultModel.success(null);
    }

    @DeleteMapping("/{id}/unlikes")
    public ResultModel<Void> unlike(
            @PathVariable("id") @NotNull(message = "参数不完整") Long id,
            @RequestAttribute(value = "userId", required = false) @NotNull(message = "参数不完整") Long userId) {
        playlistsService.toggleLike(id, userId, false);
        return ResultModel.success(null);
    }

    // 7. 歌单添加/移除歌曲
    @PostMapping("/{playlist_id}/songs/{song_id}")
    public ResultModel<AddSongToPlaylistVO> addSong(
            @PathVariable("playlist_id") @NotNull(message = "歌单ID或歌曲ID不能为空") Long playlistId,
            @PathVariable("song_id") @NotNull(message = "歌单ID或歌曲ID不能为空") Long songId) {
        return ResultModel.success(playlistsService.addSongToPlaylist(playlistId, songId));
    }

    @DeleteMapping("/{playlist_id}/songs/{song_id}")
    public ResultModel<Void> removeSong(
            @PathVariable("playlist_id") @NotNull(message = "歌单ID或歌曲ID不能为空") Long playlistId,
            @PathVariable("song_id") @NotNull(message = "歌单ID或歌曲ID不能为空") Long songId) {
        playlistsService.removeSongFromPlaylist(playlistId, songId);
        return ResultModel.success(null);
    }
}
