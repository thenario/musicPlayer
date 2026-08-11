package com.kyf.mp.javaserver.modules.songmodule.service.serviceImp;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kyf.mp.javaserver.modules.songmodule.business.ISongsBusiness;
import com.kyf.mp.javaserver.modules.songmodule.dto.EDitSongDTO;
import com.kyf.mp.javaserver.modules.songmodule.entity.Songs;
import com.kyf.mp.javaserver.modules.songmodule.service.ISongsService;
import com.kyf.mp.javaserver.modules.songmodule.vo.GetSongsVO;
import com.kyf.mp.javaserver.modules.songmodule.vo.LyricsVO;
import com.kyf.mp.javaserver.modules.songmodule.vo.UploadsVO;

import lombok.RequiredArgsConstructor;

/**
 * <p>
 * 服务实现类：业务逻辑编排，数据访问委托给 ISongsBusiness。
 * </p>
 *
 * @author kyf
 * @since 2026-04-05
 */
@Service
@RequiredArgsConstructor
public class SongsServiceImpl implements ISongsService {

    private final ISongsBusiness songsBusiness;

    @Override
    public GetSongsVO getSongsPage(Integer page, String keyword) {
        return songsBusiness.getSongsPage(page, keyword);
    }

    @Override
    public LyricsVO getLyrics(Integer songId) {
        return songsBusiness.getLyrics(songId);
    }

    @Override
    public void uploadSong(MultipartFile audioFile, MultipartFile coverFile, Integer uploaderId,
            String title, String artist, String album, String lyrics) {
        songsBusiness.uploadSong(audioFile, coverFile, uploaderId, title, artist, album, lyrics);
    }

    @Override
    public IPage<UploadsVO> getUploadSongs(Integer userId, Integer page, Integer size) {
        // 简单查询：直接用 business 的基础 CRUD
        Page<Songs> songPage = new Page<>(page, size);
        LambdaQueryWrapper<Songs> wrapper = new LambdaQueryWrapper<Songs>()
                .eq(Songs::getUploaderId, userId)
                .orderByDesc(Songs::getDateAdded);

        IPage<Songs> result = songsBusiness.page(songPage, wrapper);

        return result.convert(song -> {
            UploadsVO vo = new UploadsVO();
            BeanUtils.copyProperties(song, vo);
            return vo;
        });
    }

    @Override
    public void editUploadSong(EDitSongDTO dto, Integer userId, Integer songID) {
        songsBusiness.editUploadSong(dto, userId, songID);
    }
}
