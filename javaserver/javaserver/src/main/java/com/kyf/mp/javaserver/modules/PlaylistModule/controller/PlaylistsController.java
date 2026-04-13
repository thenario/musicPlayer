package com.kyf.mp.javaserver.modules.PlaylistModule.controller;

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

import com.kyf.mp.javaserver.common.ResultModel;
import com.kyf.mp.javaserver.modules.PlaylistModule.VO.AddSongToPlaylistVO;
import com.kyf.mp.javaserver.modules.PlaylistModule.VO.MyPlaylistsVO;
import com.kyf.mp.javaserver.modules.PlaylistModule.VO.PlaylistActionVO;
import com.kyf.mp.javaserver.modules.PlaylistModule.VO.PlaylistDetailVO;
import com.kyf.mp.javaserver.modules.PlaylistModule.service.IPlaylistsService;

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
            return ResultModel.error("请上传歌单封面", 400);
        if (name == null || name.trim().isEmpty())
            return ResultModel.error("歌单名称不能为空", 400);
        if (userId == null)
            return ResultModel.error("用户ID不能为空", 400);

        return playlistsService.createPlaylist(file, name, description, userId);
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
            return ResultModel.error("歌单ID不能为空", 400);
        if (userId == null)
            return ResultModel.error("用户ID不能为空", 400);
        // 如果提供了 name 但为空字符串
        if (name != null && name.trim().isEmpty())
            return ResultModel.error("歌单名称不能为空", 400);

        return playlistsService.editPlaylist(file, playlistId, name, description, userId);
    }

    // 3. 删除歌单
    @DeleteMapping("/{id}")
    public ResultModel<Void> deletePlaylist(
            @PathVariable("id") Integer id,
            @RequestAttribute("userId") Integer userId) {

        if (id == null)
            return ResultModel.error("歌单ID不能为空", 400);
        if (userId == null)
            return ResultModel.error("用户ID不能为空", 400);

        return playlistsService.deletePlaylist(id, userId);
    }

    // 4. 获取我的歌单列表
    @GetMapping
    public ResultModel<MyPlaylistsVO> getMyPlaylists(@RequestAttribute("userId") Integer userId) {

        if (userId == null)
            return ResultModel.error("用户ID不能为空", 400);

        return playlistsService.getMyPlaylists(userId);
    }

    // 5. 获取歌单详情（含歌曲）
    @GetMapping("/{id}")
    public ResultModel<PlaylistDetailVO> getPlaylistById(
            @PathVariable("id") Integer id,
            @RequestAttribute("userId") Integer userId) {

        if (id == null)
            return ResultModel.error("歌单ID不能为空", 400);

        return playlistsService.getPlaylistDetail(id, userId);
    }

    // 6. 点赞/取消点赞
    @PostMapping("/{id}/likes")
    public ResultModel<Void> like(
            @PathVariable("id") Integer id,
            @RequestAttribute("userId") Integer userId) {

        if (id == null || userId == null)
            return ResultModel.error("参数不完整", 400);

        return playlistsService.toggleLike(id, userId, true);
    }

    @DeleteMapping("/{id}/unlikes")
    public ResultModel<Void> unlike(
            @PathVariable("id") Integer id,
            @RequestAttribute("userId") Integer userId) {

        if (id == null || userId == null)
            return ResultModel.error("参数不完整", 400);

        return playlistsService.toggleLike(id, userId, false);
    }

    // 7. 歌单添加/移除歌曲
    @PostMapping("/{playlist_id}/songs/{song_id}")
    public ResultModel<AddSongToPlaylistVO> addSong(
            @PathVariable("playlist_id") Integer playlistId,
            @PathVariable("song_id") Integer songId) {

        if (playlistId == null || songId == null)
            return ResultModel.error("歌单ID或歌曲ID不能为空", 400);

        return playlistsService.addSongToPlaylist(playlistId, songId);
    }

    @DeleteMapping("/{playlist_id}/songs/{song_id}")
    public ResultModel<Void> removeSong(
            @PathVariable("playlist_id") Integer playlistId,
            @PathVariable("song_id") Integer songId) {

        if (playlistId == null || songId == null)
            return ResultModel.error("歌单ID或歌曲ID不能为空", 100);

        return playlistsService.removeSongFromPlaylist(playlistId, songId);
    }
}
