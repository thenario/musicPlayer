package com.kyf.mp.javaserver.service.impl;

import java.io.File;
import java.time.LocalDateTime;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kyf.mp.javaserver.common.ResultModel;
import com.kyf.mp.javaserver.entity.Songs;
import com.kyf.mp.javaserver.entity.Users;
import com.kyf.mp.javaserver.mapper.SongsMapper;
import com.kyf.mp.javaserver.mapper.UsersMapper;
import com.kyf.mp.javaserver.modelVO.GetSongsVO;
import com.kyf.mp.javaserver.modelVO.LyricsVO;
import com.kyf.mp.javaserver.service.ISongsService;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author kyf
 * @since 2026-04-05
 */
@Service
@Slf4j
public class SongsServiceImpl extends ServiceImpl<SongsMapper, Songs> implements ISongsService {
    public ResultModel<GetSongsVO> getSongsPage(Integer page, String keyword) {
        int pageLimit = 15;

        Page<Songs> pageConfig = new Page<>(page, pageLimit);

        LambdaQueryWrapper<Songs> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(Songs.class, info -> !info.getColumn().equalsIgnoreCase("lyrics") &&
                !info.getColumn().equalsIgnoreCase("t_lyrics"));
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Songs::getSongTitle, keyword);
        }

        Page<Songs> result = baseMapper.selectPage(pageConfig, wrapper);

        GetSongsVO vo = new GetSongsVO();
        vo.setSongs(result.getRecords());
        vo.setPagination(new GetSongsVO.PaginationVO(
                result.getTotal(),
                result.getPages(),
                result.getCurrent(),
                pageLimit));

        return ResultModel.success(vo);
    }

    public ResultModel<LyricsVO> getLyrics(Integer songId) {

        Songs song = baseMapper.selectOne(
                new LambdaQueryWrapper<Songs>()
                        .select(Songs::getLyrics, Songs::getTLyrics)
                        .eq(Songs::getSongId, songId));

        if (song == null) {
            return ResultModel.error("未找到该歌曲", 404);
        }

        LyricsVO vo = new LyricsVO();
        vo.setLyrics(song.getLyrics() != null ? song.getLyrics() : "");
        vo.setTLyrics(song.getTLyrics() != null ? song.getTLyrics() : "");

        return ResultModel.success(vo);
    }

    @Value("${file.upload.song-path}")
    private String songPath;

    @Value("${file.upload.song-cover-path}")
    private String songCoverPath;

    @Value("${file.static.song-url}")
    private String songUrl;

    @Value("${file.static.song-cover-url}")
    private String songCoverUrl;

    @Autowired
    private UsersMapper userMapper;

    public ResultModel<Void> uploadSong(MultipartFile audioFile, MultipartFile coverFile, Integer uploaderId,
            String title, String artist, String album, String lyrics) {

        String audioFolder = songPath;
        String coverFolder = songCoverPath;

        File savedAudioFile = null;
        File savedCoverFile = null;

        try {
            String audioExt = StringUtils.getFilenameExtension(audioFile.getOriginalFilename());
            String coverExt = StringUtils.getFilenameExtension(coverFile.getOriginalFilename());

            String audioName = "audiofile-" + System.currentTimeMillis() + "." + audioExt;
            String coverName = "coverfile-" + System.currentTimeMillis() + "." + coverExt;

            savedAudioFile = new File(audioFolder + audioName);
            savedCoverFile = new File(coverFolder + coverName);

            savedAudioFile.getParentFile().mkdirs();
            savedCoverFile.getParentFile().mkdirs();

            audioFile.transferTo(savedAudioFile);
            coverFile.transferTo(savedCoverFile);

            AudioFile f = AudioFileIO.read(savedAudioFile);
            AudioHeader header = f.getAudioHeader();
            int duration = header.getTrackLength();
            long bitrate = header.getBitRateAsNumber();

            Users user = userMapper.selectById(uploaderId);
            if (user == null) {
                throw new RuntimeException("上传用户不存在");
            }

            Songs song = new Songs();
            song.setUploaderId(uploaderId);
            song.setUploaderName(user.getUserName());
            song.setSongTitle(StringUtils.hasText(title) ? title : audioFile.getOriginalFilename());
            song.setArtist(StringUtils.hasText(artist) ? artist : "未知艺术家");
            song.setAlbum(StringUtils.hasText(album) ? album : "未知专辑");
            song.setFileSize(String.valueOf(audioFile.getSize()));
            song.setDuration(duration);
            song.setBitrate((int) bitrate);
            song.setFileFormat(audioExt);
            song.setLyrics(lyrics);

            song.setSongUrl(songUrl + audioName);
            song.setSongCoverUrl(songCoverUrl + coverName);
            song.setDateAdded(LocalDateTime.now());

            baseMapper.insert(song);

            return ResultModel.success(null);

        } catch (Exception e) {
            if (savedAudioFile != null && savedAudioFile.exists())
                savedAudioFile.delete();
            if (savedCoverFile != null && savedCoverFile.exists())
                savedCoverFile.delete();

            log.error("上传失败: ", e);
            return ResultModel.error("提交出错，数据库写入失败", 400);
        }
    }
}
