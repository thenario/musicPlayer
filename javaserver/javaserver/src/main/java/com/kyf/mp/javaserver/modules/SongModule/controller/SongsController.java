package com.kyf.mp.javaserver.modules.SongModule.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.kyf.mp.javaserver.common.ResultModel;
import com.kyf.mp.javaserver.modules.SongModule.VO.GetSongsVO;
import com.kyf.mp.javaserver.modules.SongModule.VO.LyricsVO;
import com.kyf.mp.javaserver.modules.SongModule.service.ISongsService;

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
@RequestMapping("/api/songs")
@RequiredArgsConstructor
public class SongsController {
    private final ISongsService songsService;

    @GetMapping
    public ResultModel<GetSongsVO> getSongs(@RequestParam(defaultValue = "1") Integer page,
            @RequestParam(required = false) String keyword) {
        if (page < 1) {
            return ResultModel.error("请输入正确页码", 400);
        }
        return songsService.getSongsPage(page, keyword);
    }

    // 获取歌词

    @GetMapping("/{song_id}/lyrics")
    public ResultModel<LyricsVO> getLyrics(@PathVariable("song_id") Integer songId) {
        if (songId == null) {
            return ResultModel.error("缺少歌曲ID", 400);
        }
        return songsService.getLyrics(songId);
    }

    // 上传歌曲
    @PostMapping
    public ResultModel<Void> upload(@RequestParam("audiofile") MultipartFile audioFile,
            @RequestParam("coverfile") MultipartFile coverFile,
            @RequestParam("uploader_id") Integer uploaderId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String artist,
            @RequestParam(required = false) String album,
            @RequestParam(required = false) String lyrics) {
        if (audioFile == null || coverFile == null || uploaderId == null) {
            return ResultModel.error("请上传完整信息(包含封面与音频文件)", 400);
        }
        return songsService.uploadSong(audioFile, coverFile, uploaderId, title, artist, album, lyrics);
    }
}
