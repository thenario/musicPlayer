package com.kyf.mp.server.modules.song.service;

import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.kyf.mp.server.modules.song.dto.EditSongDTO;
import com.kyf.mp.server.modules.song.vo.GetSongsVO;
import com.kyf.mp.server.modules.song.vo.LyricsVO;
import com.kyf.mp.server.modules.song.vo.UploadsVO;

public interface SongsService {
    GetSongsVO getSongsPage(Integer page, String keyword);
    LyricsVO getLyrics(Long songId);
    void uploadSong(MultipartFile audioFile, MultipartFile coverFile, Long userId, String title, String artist, String album, String lyrics);
    IPage<UploadsVO> getUploadSongs(Long userId, Integer page, Integer size);
    UploadsVO getUploadSong(Long userId, Long songId);
    void editUploadSong(EditSongDTO dto, Long userId, Long songID);
}