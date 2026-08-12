package com.kyf.mp.javaserver.modules.songmodule.controller;

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
import com.kyf.mp.javaserver.common.ResultModel;
import com.kyf.mp.javaserver.modules.songmodule.dto.EDitSongDTO;
import com.kyf.mp.javaserver.modules.songmodule.vo.GetSongsVO;
import com.kyf.mp.javaserver.modules.songmodule.vo.LyricsVO;
import com.kyf.mp.javaserver.modules.songmodule.vo.UploadsVO;
import com.kyf.mp.javaserver.modules.songmodule.service.ISongsService;

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
public class SongsController {
    private final ISongsService songsService;

    @GetMapping
    public ResultModel<GetSongsVO> getSongs(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "请输入正确页码") Integer page,
            @RequestParam(required = false) String keyword) {
        return ResultModel.success(songsService.getSongsPage(page, keyword));
    }

    // 获取歌词

    @GetMapping("/{song_id}/lyrics")
    public ResultModel<LyricsVO> getLyrics(
            @PathVariable("song_id") @NotNull(message = "缺少歌曲ID") Long songId) {
        return ResultModel.success(songsService.getLyrics(songId));
    }

    // 上传歌曲
    @PostMapping
    public ResultModel<Void> upload(
            @RequestParam("audiofile") @NotNull(message = "请上传完整信息(包含封面与音频文件)") MultipartFile audioFile,
            @RequestParam("coverfile") @NotNull(message = "请上传完整信息(包含封面与音频文件)") MultipartFile coverFile,
            @RequestParam("uploader_id") @NotNull(message = "请上传完整信息(包含封面与音频文件)") Long uploaderId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String artist,
            @RequestParam(required = false) String album,
            @RequestParam(required = false) String lyrics) {
        songsService.uploadSong(audioFile, coverFile, uploaderId, title, artist, album, lyrics);
        return ResultModel.success(null);
    }

    @GetMapping("/my-uploads")
    public ResultModel<IPage<UploadsVO>> getUploadSongs(@RequestAttribute("userId") Long userId,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "请输入正确页码") Integer page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页条数不正确") @Max(value = 100, message = "每页条数过大") Integer size) {
        return ResultModel.success(songsService.getUploadSongs(userId, page, size));
    }

    @PatchMapping("/my-uploads/{song_id}")
    public ResultModel<Void> editUploadSong(EDitSongDTO dto, @RequestAttribute("userId") Long userId,
            @PathVariable("song_id") @NotNull(message = "缺少歌曲ID") Long songId) {
        songsService.editUploadSong(dto, userId, songId);
        return ResultModel.success(null);
    }

}
