package com.kyf.mp.server.modules.song.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.kyf.mp.server.common.ResultModel;
import com.kyf.mp.server.modules.song.dto.EditSongDTO;
import com.kyf.mp.server.modules.song.vo.GetSongsVO;
import com.kyf.mp.server.modules.song.vo.LyricsVO;
import com.kyf.mp.server.modules.song.vo.UploadsVO;
import com.kyf.mp.server.modules.song.service.SongsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.constraints.Max;
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
@RequestMapping("/api/songs")
@RequiredArgsConstructor
@Tag(name = "歌曲", description = "歌曲浏览、歌词查询和上传管理")
public class SongsController {
    private final SongsService songsService;

    @GetMapping
    @Operation(summary = "获取歌曲列表", description = "分页获取歌曲；可使用关键词搜索歌曲名称、歌手或专辑")
    public ResultModel<GetSongsVO> getSongs(
            @Parameter(description = "页码，从 1 开始", example = "1")
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "请输入正确页码") Integer page,
            @Parameter(description = "搜索关键词", example = "周杰伦")
            @RequestParam(required = false) String keyword) {
        return ResultModel.success(songsService.getSongsPage(page, keyword));
    }

    @GetMapping("/{song_id}/lyrics")
    @Operation(summary = "获取歌曲歌词", description = "返回歌曲原始歌词和翻译歌词")
    public ResultModel<LyricsVO> getLyrics(
            @Parameter(description = "歌曲 ID", required = true, example = "1")
            @PathVariable("song_id") @NotNull(message = "缺少歌曲ID") @Min(value = 1, message = "Song ID must be positive") Long songId) {
        return ResultModel.success(songsService.getLyrics(songId));
    }

    @PostMapping
    @Operation(summary = "上传歌曲", description = "上传音频文件、封面及可选的歌曲信息")
    @SecurityRequirement(name = "bearerAuth")
    public ResultModel<Void> upload(
            @Parameter(description = "音频文件", required = true)
            @RequestParam("audiofile") @NotNull(message = "请上传完整信息(包含封面与音频文件)") MultipartFile audioFile,
            @Parameter(description = "歌曲封面图片", required = true)
            @RequestParam("coverfile") @NotNull(message = "请上传完整信息(包含封面与音频文件)") MultipartFile coverFile,
            @Parameter(hidden = true)
            @RequestAttribute("userId") Long userId,
            @Parameter(description = "歌曲名称")
            @RequestParam(required = false) String title,
            @Parameter(description = "歌手名称")
            @RequestParam(required = false) String artist,
            @Parameter(description = "专辑名称")
            @RequestParam(required = false) String album,
            @Parameter(description = "歌词内容")
            @RequestParam(required = false) String lyrics) {
        songsService.uploadSong(audioFile, coverFile, userId, title, artist, album, lyrics);
        return ResultModel.success(null);
    }

    @GetMapping("/my-uploads")
    @Operation(summary = "获取我的上传歌曲", description = "分页获取当前用户上传的歌曲")
    @SecurityRequirement(name = "bearerAuth")
    public ResultModel<IPage<UploadsVO>> getUploadSongs(
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId,
            @Parameter(description = "页码，从 1 开始", example = "1")
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "请输入正确页码") Integer page,
            @Parameter(description = "每页数量，最大 100", example = "10")
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页条数不正确") @Max(value = 100, message = "每页条数过大") Integer size) {
        return ResultModel.success(songsService.getUploadSongs(userId, page, size));
    }

    @GetMapping("/my-uploads/{song_id}")
    @Operation(summary = "获取我的上传歌曲详情")
    @SecurityRequirement(name = "bearerAuth")
    public ResultModel<UploadsVO> getUploadSong(
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId,
            @Parameter(description = "歌曲 ID", required = true, example = "1")
            @PathVariable("song_id") @NotNull(message = "缺少歌曲ID") @Min(value = 1, message = "Song ID must be positive") Long songId) {
        return ResultModel.success(songsService.getUploadSong(userId, songId));
    }

    @PatchMapping("/my-uploads/{song_id}")
    @Operation(summary = "编辑我的上传歌曲", description = "修改当前用户上传歌曲的名称、歌词或封面")
    @SecurityRequirement(name = "bearerAuth")
    public ResultModel<Void> editUploadSong(EditSongDTO dto,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId,
            @Parameter(description = "歌曲 ID", required = true, example = "1")
            @PathVariable("song_id") @NotNull(message = "缺少歌曲ID") @Min(value = 1, message = "Song ID must be positive") Long songId) {
        songsService.editUploadSong(dto, userId, songId);
        return ResultModel.success(null);
    }

}
