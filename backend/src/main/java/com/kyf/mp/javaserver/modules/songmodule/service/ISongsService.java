package com.kyf.mp.javaserver.modules.songmodule.service;

import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kyf.mp.javaserver.common.ResultModel;
import com.kyf.mp.javaserver.modules.songmodule.dto.EDitSongDTO;
import com.kyf.mp.javaserver.modules.songmodule.vo.GetSongsVO;
import com.kyf.mp.javaserver.modules.songmodule.vo.LyricsVO;
import com.kyf.mp.javaserver.modules.songmodule.vo.UploadsVO;
import com.kyf.mp.javaserver.modules.songmodule.entity.Songs;

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

    ResultModel<IPage<UploadsVO>> getUploadSongs(Integer userId, Integer page, Integer size);

    ResultModel<Void> editUploadSong(EDitSongDTO dto, Integer userId, Integer songID);
}
