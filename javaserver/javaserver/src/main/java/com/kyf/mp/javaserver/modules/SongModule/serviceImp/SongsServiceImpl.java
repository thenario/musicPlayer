package com.kyf.mp.javaserver.modules.SongModule.serviceImp;

import java.io.File;
import java.time.LocalDateTime;
import java.util.UUID;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kyf.mp.javaserver.common.BusinessException;
import com.kyf.mp.javaserver.common.ResultModel;
import com.kyf.mp.javaserver.modules.SongModule.VO.GetSongsVO;
import com.kyf.mp.javaserver.modules.SongModule.VO.LyricsVO;
import com.kyf.mp.javaserver.modules.SongModule.entity.Songs;
import com.kyf.mp.javaserver.modules.SongModule.mapper.SongsMapper;
import com.kyf.mp.javaserver.modules.SongModule.service.ISongsService;
import com.kyf.mp.javaserver.modules.UserModule.entity.Users;
import com.kyf.mp.javaserver.modules.UserModule.mapper.UsersMapper;

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

    @Override
    public ResultModel<GetSongsVO> getSongsPage(Integer page, String keyword) {
        int pageLimit = 15;
        Page<Songs> pageConfig = new Page<>(page, pageLimit);

        LambdaQueryWrapper<Songs> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(Songs.class, info -> !info.getColumn().equalsIgnoreCase("lyrics") &&
                !info.getColumn().equalsIgnoreCase("t_lyrics"));

        if (StringUtils.hasText(keyword)) {
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

    @Override
    public ResultModel<LyricsVO> getLyrics(Integer songId) {
        Songs song = baseMapper.selectOne(
                new LambdaQueryWrapper<Songs>()
                        .select(Songs::getLyrics, Songs::getTLyrics)
                        .eq(Songs::getSongId, songId));

        if (song == null) {
            throw new BusinessException(404, "未找到该歌曲或歌词已下架");
        }

        LyricsVO vo = new LyricsVO();
        vo.setLyrics(StringUtils.hasText(song.getLyrics()) ? song.getLyrics() : "");
        vo.setTLyrics(StringUtils.hasText(song.getTLyrics()) ? song.getTLyrics() : "");

        return ResultModel.success(vo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultModel<Void> uploadSong(MultipartFile audioFile, MultipartFile coverFile, Integer uploaderId,
            String title, String artist, String album, String lyrics) {

        File savedAudioFile = null;
        File savedCoverFile = null;

        try {
            String audioExt = StringUtils.getFilenameExtension(audioFile.getOriginalFilename());
            String coverExt = StringUtils.getFilenameExtension(coverFile.getOriginalFilename());

            String audioName = "audiofile-" + UUID.randomUUID() + "." + audioExt;
            String coverName = "coverfile-" + UUID.randomUUID() + "." + coverExt;

            savedAudioFile = new File(songPath, audioName);
            savedCoverFile = new File(songCoverPath, coverName);

            if (!savedAudioFile.getParentFile().exists())
                savedAudioFile.getParentFile().mkdirs();
            if (!savedCoverFile.getParentFile().exists())
                savedCoverFile.getParentFile().mkdirs();

            audioFile.transferTo(savedAudioFile);
            coverFile.transferTo(savedCoverFile);

            AudioFile f = AudioFileIO.read(savedAudioFile);
            AudioHeader header = f.getAudioHeader();
            int duration = header.getTrackLength();
            long bitrate = header.getBitRateAsNumber();

            Users user = userMapper.selectById(uploaderId);
            if (user == null) {
                throw new BusinessException(401, "上传失败：上传者身份无效");
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

        } catch (BusinessException be) {
            cleanupFiles(savedAudioFile, savedCoverFile);
            throw be;
        } catch (Exception e) {

            cleanupFiles(savedAudioFile, savedCoverFile);
            log.error("上传歌曲发生系统错误: ", e);
            throw new BusinessException(500, "文件处理异常: " + e.getMessage());
        }
    }

    private void cleanupFiles(File audio, File cover) {
        if (audio != null && audio.exists()) {
            boolean d1 = audio.delete();
            log.info("清理异常音频文件: {}", d1);
        }
        if (cover != null && cover.exists()) {
            boolean d2 = cover.delete();
            log.info("清理异常封面文件: {}", d2);
        }
    }
}