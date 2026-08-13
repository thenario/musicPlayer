package com.kyf.mp.javaserver.modules.songmodule.business;

import org.springframework.web.multipart.MultipartFile;

import com.kyf.mp.javaserver.common.business.BaseBusiness;
import com.kyf.mp.javaserver.modules.songmodule.dto.EDitSongDTO;
import com.kyf.mp.javaserver.modules.songmodule.entity.Songs;
import com.kyf.mp.javaserver.modules.songmodule.vo.GetSongsVO;
import com.kyf.mp.javaserver.modules.songmodule.vo.LyricsVO;

/**
 * 歌曲数据访问层：复杂数据库操作在此定义。
 */
public interface SongsBusiness extends BaseBusiness<Songs> {
    GetSongsVO getSongsPage(Integer page, String keyword);

    LyricsVO getLyrics(Long songId);

    void uploadSong(MultipartFile audioFile, MultipartFile coverFile, Long uploaderId,
            String title, String artist, String album, String lyrics);

    void editUploadSong(EDitSongDTO dto, Long userId, Long songID);
}
