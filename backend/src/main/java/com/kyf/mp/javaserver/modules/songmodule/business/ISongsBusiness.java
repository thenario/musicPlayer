package com.kyf.mp.javaserver.modules.songmodule.business;

import org.springframework.web.multipart.MultipartFile;

import com.kyf.mp.javaserver.common.ResultModel;
import com.kyf.mp.javaserver.common.business.IBaseBusiness;
import com.kyf.mp.javaserver.modules.songmodule.dto.EDitSongDTO;
import com.kyf.mp.javaserver.modules.songmodule.entity.Songs;
import com.kyf.mp.javaserver.modules.songmodule.vo.GetSongsVO;
import com.kyf.mp.javaserver.modules.songmodule.vo.LyricsVO;

/**
 * 歌曲数据访问层：复杂数据库操作在此定义。
 */
public interface ISongsBusiness extends IBaseBusiness<Songs> {
    ResultModel<GetSongsVO> getSongsPage(Integer page, String keyword);

    ResultModel<LyricsVO> getLyrics(Integer songId);

    ResultModel<Void> uploadSong(MultipartFile audioFile, MultipartFile coverFile, Integer uploaderId,
            String title, String artist, String album, String lyrics);

    ResultModel<Void> editUploadSong(EDitSongDTO dto, Integer userId, Integer songID);
}
