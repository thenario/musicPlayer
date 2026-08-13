package com.kyf.mp.javaserver.modules.songmodule.service;

import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.kyf.mp.javaserver.modules.songmodule.dto.EDitSongDTO;
import com.kyf.mp.javaserver.modules.songmodule.vo.GetSongsVO;
import com.kyf.mp.javaserver.modules.songmodule.vo.LyricsVO;
import com.kyf.mp.javaserver.modules.songmodule.vo.UploadsVO;

/**
 * <p>
 * 服务类：业务逻辑层，不再继承 IService（基础 CRUD 由 business 层提供）。
 * </p>
 *
 * @author kyf
 * @since 2026-04-05
 */
public interface SongsService {
    GetSongsVO getSongsPage(Integer page, String keyword);

    LyricsVO getLyrics(Long songId);

    void uploadSong(MultipartFile audioFile, MultipartFile coverFile, Long uploaderId,
            String title, String artist, String album, String lyrics);

    IPage<UploadsVO> getUploadSongs(Long userId, Integer page, Integer size);

    void editUploadSong(EDitSongDTO dto, Long userId, Long songID);
}
