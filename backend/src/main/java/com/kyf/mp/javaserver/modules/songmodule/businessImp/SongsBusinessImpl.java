package com.kyf.mp.javaserver.modules.songmodule.businessImp;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kyf.mp.javaserver.common.BusinessException;
import com.kyf.mp.javaserver.common.ResultModel;
import com.kyf.mp.javaserver.common.business.BaseBusinessImpl;
import com.kyf.mp.javaserver.modules.songmodule.business.ISongsBusiness;
import com.kyf.mp.javaserver.modules.songmodule.dto.EDitSongDTO;
import com.kyf.mp.javaserver.modules.songmodule.entity.Songs;
import com.kyf.mp.javaserver.modules.songmodule.mapper.SongsMapper;
import com.kyf.mp.javaserver.modules.songmodule.vo.GetSongsVO;
import com.kyf.mp.javaserver.modules.songmodule.vo.LyricsVO;
import com.kyf.mp.javaserver.modules.usermodule.entity.Users;
import com.kyf.mp.javaserver.modules.usermodule.mapper.UsersMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 歌曲数据访问实现：复杂数据库操作。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SongsBusinessImpl extends BaseBusinessImpl<SongsMapper, Songs> implements ISongsBusiness {

    private final UsersMapper userMapper;

    @Value("${file.upload.song-path}")
    private String songPath;

    @Value("${file.upload.song-cover-path}")
    private String songCoverPath;

    @Value("${file.static.song-url}")
    private String songUrl;

    @Value("${file.static.song-cover-url}")
    private String songCoverUrl;

    @Override
    public ResultModel<GetSongsVO> getSongsPage(Integer page, String keyword) {
        int current = (page == null || page < 1) ? 1 : page;
        int pageLimit = 15;

        Page<Songs> pageConfig = new Page<>(current, pageLimit);

        LambdaQueryWrapper<Songs> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(Songs.class, info -> !info.getColumn().equalsIgnoreCase("lyrics") &&
                !info.getColumn().equalsIgnoreCase("t_lyrics"));

        if (StringUtils.hasText(keyword)) {
            wrapper.like(Songs::getSongTitle, keyword);
        }

        wrapper.orderByDesc(Songs::getDateAdded);

        var result = baseMapper.selectPage(pageConfig, wrapper);

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

            String finalTitle = StringUtils.hasText(title) ? title : "Unknown Title";
            String finalArtist = StringUtils.hasText(artist) ? artist : "Unknown Artist";
            String finalAlbum = StringUtils.hasText(album) ? album : "Unknown Album";

            String safeName = (finalArtist + " - " + finalTitle).replaceAll("[\\\\/:*?\"<>|]", "_");
            String audioName = safeName + "_" + UUID.randomUUID().toString().substring(0, 8) + "." + audioExt;
            String coverName = "cover-" + UUID.randomUUID().toString().substring(0, 8) + "." + coverExt;

            savedAudioFile = new File(songPath, audioName);
            savedCoverFile = new File(songCoverPath, coverName);

            if (!savedAudioFile.getParentFile().exists())
                savedAudioFile.getParentFile().mkdirs();
            if (!savedCoverFile.getParentFile().exists())
                savedCoverFile.getParentFile().mkdirs();

            audioFile.transferTo(savedAudioFile);
            if (coverFile != null && !coverFile.isEmpty()) {
                coverFile.transferTo(savedCoverFile);
            }

            AudioFile f = AudioFileIO.read(savedAudioFile);
            Tag tag = f.getTagOrCreateAndSetDefault();

            tag.setField(FieldKey.TITLE, finalTitle);
            tag.setField(FieldKey.ARTIST, finalArtist);
            tag.setField(FieldKey.ALBUM, finalAlbum);
            if (StringUtils.hasText(lyrics)) {
                tag.setField(FieldKey.LYRICS, lyrics);
            }

            f.commit();

            AudioHeader header = f.getAudioHeader();
            int duration = header.getTrackLength();
            long bitrate = header.getBitRateAsNumber();

            String fileMd5 = calculateMD5(savedAudioFile);

            Users user = userMapper.selectById(uploaderId);
            if (user == null)
                throw new BusinessException(401, "上传失败：上传者身份无效");

            Songs song = new Songs();
            song.setFileMd5(fileMd5);
            song.setUploaderId(uploaderId);
            song.setUploaderName(user.getUserName());
            song.setSongTitle(finalTitle);
            song.setArtist(finalArtist);
            song.setAlbum(finalAlbum);
            song.setFileSize(String.valueOf(savedAudioFile.length()));
            song.setDuration(duration);
            song.setBitrate((int) bitrate);
            song.setFileFormat(audioExt);
            song.setLyrics(lyrics);
            song.setSongUrl(songUrl + audioName);
            song.setSongCoverUrl((coverFile != null) ? (songCoverUrl + coverName) : null);
            song.setDateAdded(LocalDateTime.now());

            baseMapper.insert(song);
            return ResultModel.success(null);

        } catch (Exception e) {
            cleanupFiles(savedAudioFile, savedCoverFile);
            log.error("上传歌曲失败: ", e);
            throw new BusinessException(500, "上传失败: " + e.getMessage());
        }
    }

    private String calculateMD5(File file) throws IOException {
        try (java.io.FileInputStream fis = new java.io.FileInputStream(file)) {
            return org.springframework.util.DigestUtils.md5DigestAsHex(fis);
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultModel<Void> editUploadSong(EDitSongDTO dto, Integer userId, Integer songId) {
        Songs oldSong = baseMapper.selectById(songId);
        if (oldSong == null)
            throw new BusinessException(404, "歌曲不存在");

        if (!oldSong.getUploaderId().equals(userId)) {
            throw new BusinessException(403, "你没有权限修改这首歌");
        }

        Songs updateEntity = new Songs();
        updateEntity.setSongId(songId);
        boolean needUpdate = false;

        if (StringUtils.hasText(dto.getSong_name())) {
            updateEntity.setSongTitle(dto.getSong_name());
            needUpdate = true;
        }
        if (dto.getLyrics() != null) {
            updateEntity.setLyrics(dto.getLyrics());
            needUpdate = true;
        }

        MultipartFile newCover = dto.getSong_cover();
        if (newCover != null && !newCover.isEmpty()) {
            String ext = StringUtils.getFilenameExtension(newCover.getOriginalFilename());
            String newFileName = "cover-" + UUID.randomUUID().toString().substring(0, 8) + "." + ext;
            File destFile = new File(songCoverPath, newFileName);

            try {
                if (!destFile.getParentFile().exists())
                    destFile.getParentFile().mkdirs();
                newCover.transferTo(destFile);

                String oldUrl = oldSong.getSongCoverUrl();
                if (StringUtils.hasText(oldUrl)) {
                    String oldFileName = oldUrl.substring(oldUrl.lastIndexOf("/") + 1);
                    File oldFile = new File(songCoverPath, oldFileName);
                    if (oldFile.exists() && oldFile.isFile()) {
                        oldFile.delete();
                        log.info("用户ID: {} 修改歌曲，已清理旧封面: {}", userId, oldFileName);
                    }
                }

                updateEntity.setSongCoverUrl(songCoverUrl + newFileName);
                needUpdate = true;
            } catch (IOException e) {
                log.error("封面物理保存失败", e);
                throw new BusinessException(500, "文件保存失败");
            }
        }

        if (needUpdate) {
            baseMapper.updateById(updateEntity);
        }

        return ResultModel.success(null);
    }
}
