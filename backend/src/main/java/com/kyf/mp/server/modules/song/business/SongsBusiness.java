package com.kyf.mp.server.modules.song.business;

import org.springframework.web.multipart.MultipartFile;

import com.kyf.mp.server.common.business.BaseBusiness;
import com.kyf.mp.server.modules.song.dto.EditSongDTO;
import com.kyf.mp.server.modules.song.entity.Songs;
import com.kyf.mp.server.modules.song.vo.GetSongsVO;
import com.kyf.mp.server.modules.song.vo.LyricsVO;

/**                                             
 * 歌曲数据访问层：复杂数据库操作在此定义。
 */
public interface SongsBusiness extends BaseBusiness<Songs> {
    GetSongsVO getSongsPage(Integer page, String keyword);

    LyricsVO getLyrics(Long songId);

    void uploadSong(MultipartFile audioFile, MultipartFile coverFile, Long userId,
            String title, String artist, String album, String lyrics);

    void editUploadSong(EditSongDTO dto, Long userId, Long songID);
}
