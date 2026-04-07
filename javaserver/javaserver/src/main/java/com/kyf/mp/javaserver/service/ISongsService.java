package com.kyf.mp.javaserver.service;

import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kyf.mp.javaserver.common.ResultModel;
import com.kyf.mp.javaserver.entity.Songs;
import com.kyf.mp.javaserver.modelVO.GetSongsVO;
import com.kyf.mp.javaserver.modelVO.LyricsVO;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author kyf
 * @since 2026-04-05
 */
public interface ISongsService extends IService<Songs> {
    ResultModel<GetSongsVO> getSongsPage(Integer page, String keyword);

    ResultModel<LyricsVO> getLyrics(Integer songId);

    ResultModel<Void> uploadSong(MultipartFile audioFile, MultipartFile coverFile, Integer uploaderId,
            String title, String artist, String album, String lyrics);
}
