package com.kyf.mp.javaserver.modules.playlistmodule.controller;

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

import com.kyf.mp.javaserver.common.BusinessException;
import com.kyf.mp.javaserver.common.ResultModel;
import com.kyf.mp.javaserver.modules.playlistmodule.vo.AddSongToPlaylistVO;
import com.kyf.mp.javaserver.modules.playlistmodule.vo.MyPlaylistsVO;
import com.kyf.mp.javaserver.modules.playlistmodule.vo.PlaylistActionVO;
import com.kyf.mp.javaserver.modules.playlistmodule.vo.PlaylistDetailVO;
import com.kyf.mp.javaserver.modules.playlistmodule.service.IPlaylistsService;

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
@RequestMapping("/api/playlists")
@RequiredArgsConstructor
public class PlaylistsController {
    private final IPlaylistsService playlistsService;

    // 1. 创建歌单
    @PostMapping
    public ResultModel<PlaylistActionVO> createPlaylist(
            @RequestParam("cover_image") MultipartFile file,
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestAttribute("userId") Integer userId) {

        // 参数检查
        if (file == null || file.isEmpty())
            throw new BusinessException(400, "请上传歌单封面");
        if (name == null || name.trim().isEmpty())
            throw new BusinessException(400, "歌单名称不能为空");
        if (userId == null)
            throw new BusinessException(400, "用户ID不能为空");

        return ResultModel.success(playlistsService.createPlaylist(file, name, description, userId));
    }

    // 2. 编辑歌单详情
    @PatchMapping
    public ResultModel<PlaylistActionVO> editPlaylist(
            @RequestParam(value = "cover_image", required = false) MultipartFile file,
            @RequestParam("playlist_id") Integer playlistId,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestAttribute("userId") Integer userId) {

        // 参数检查
        if (playlistId == null)
            throw new BusinessException(400, "歌单ID不能为空");
        if (userId == null)
            throw new BusinessException(400, "用户ID不能为空");
        // 如果提供了 name 但为空字符串
        if (name != null && name.trim().isEmpty())
            throw new BusinessException(400, "歌单名称不能为空");

        return ResultModel.success(playlistsService.editPlaylist(file, playlistId, name, description, userId));
    }

    // 3. 删除歌单
    @DeleteMapping("/{id}")
    public ResultModel<Void> deletePlaylist(
            @PathVariable("id") Integer id,
            @RequestAttribute("userId") Integer userId) {

        if (id == null)
            throw new BusinessException(400, "歌单ID不能为空");
        if (userId == null)
            throw new BusinessException(400, "用户ID不能为空");

        playlistsService.deletePlaylist(id, userId);
        return ResultModel.success(null);
    }

    // 4. 获取我的歌单列表
    @GetMapping
    public ResultModel<MyPlaylistsVO> getMyPlaylists(@RequestAttribute("userId") Integer userId) {

        if (userId == null)
            throw new BusinessException(400, "用户ID不能为空");

        return ResultModel.success(playlistsService.getMyPlaylists(userId));
    }

    // 5. 获取歌单详情（含歌曲）
    @GetMapping("/{id}")
    public ResultModel<PlaylistDetailVO> getPlaylistById(
            @PathVariable("id") Integer id,
            @RequestAttribute("userId") Integer userId) {

        if (id == null)
            throw new BusinessException(400, "歌单ID不能为空");

        return ResultModel.success(playlistsService.getPlaylistDetail(id, userId));
    }

    // 6. 点赞/取消点赞
    @PostMapping("/{id}/likes")
    public ResultModel<Void> like(
            @PathVariable("id") Integer id,
            @RequestAttribute("userId") Integer userId) {

        if (id == null || userId == null)
            throw new BusinessException(400, "参数不完整");

        playlistsService.toggleLike(id, userId, true);
        return ResultModel.success(null);
    }

    @DeleteMapping("/{id}/unlikes")
    public ResultModel<Void> unlike(
            @PathVariable("id") Integer id,
            @RequestAttribute("userId") Integer userId) {

        if (id == null || userId == null)
            throw new BusinessException(400, "参数不完整");

        playlistsService.toggleLike(id, userId, false);
        return ResultModel.success(null);
    }

    // 7. 歌单添加/移除歌曲
    @PostMapping("/{playlist_id}/songs/{song_id}")
    public ResultModel<AddSongToPlaylistVO> addSong(
            @PathVariable("playlist_id") Integer playlistId,
            @PathVariable("song_id") Integer songId) {

        if (playlistId == null || songId == null)
            throw new BusinessException(400, "歌单ID或歌曲ID不能为空");

        return ResultModel.success(playlistsService.addSongToPlaylist(playlistId, songId));
    }

    @DeleteMapping("/{playlist_id}/songs/{song_id}")
    public ResultModel<Void> removeSong(
            @PathVariable("playlist_id") Integer playlistId,
            @PathVariable("song_id") Integer songId) {

        if (playlistId == null || songId == null)
            throw new BusinessException(400, "歌单ID或歌曲ID不能为空");

        playlistsService.removeSongFromPlaylist(playlistId, songId);
        return ResultModel.success(null);
    }
}
