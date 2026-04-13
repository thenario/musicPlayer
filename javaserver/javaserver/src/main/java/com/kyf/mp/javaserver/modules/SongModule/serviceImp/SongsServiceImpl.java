package com.kyf.mp.javaserver.modules.SongModule.serviceImp;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.jaudiotagger.tag.Tag;

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
        // 严谨性检查：如果 page 是空或者是负数，默认为 1
        int current = (page == null || page < 1) ? 1 : page;
        int pageLimit = 15;

        Page<Songs> pageConfig = new Page<>(current, pageLimit);

        LambdaQueryWrapper<Songs> wrapper = new LambdaQueryWrapper<>();
        // 排除歌词字段（性能优化）
        wrapper.select(Songs.class, info -> !info.getColumn().equalsIgnoreCase("lyrics") &&
                !info.getColumn().equalsIgnoreCase("t_lyrics"));

        if (StringUtils.hasText(keyword)) {
            wrapper.like(Songs::getSongTitle, keyword);
        }

        // 加上排序，防止分页数据在多次请求间顺序错乱
        wrapper.orderByDesc(Songs::getDateAdded);

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
            // 1. 基本信息处理
            String audioExt = StringUtils.getFilenameExtension(audioFile.getOriginalFilename());
            String coverExt = StringUtils.getFilenameExtension(coverFile.getOriginalFilename());

            // 确定最终的标题和歌手（用于文件名和标签）
            String finalTitle = StringUtils.hasText(title) ? title : "Unknown Title";
            String finalArtist = StringUtils.hasText(artist) ? artist : "Unknown Artist";
            String finalAlbum = StringUtils.hasText(album) ? album : "Unknown Album";

            // 2. 生成可读性强的文件名 (过滤掉系统非法字符)
            String safeName = (finalArtist + " - " + finalTitle).replaceAll("[\\\\/:*?\"<>|]", "_");
            String audioName = safeName + "_" + UUID.randomUUID().toString().substring(0, 8) + "." + audioExt;
            String coverName = "cover-" + UUID.randomUUID().toString().substring(0, 8) + "." + coverExt;

            savedAudioFile = new File(songPath, audioName);
            savedCoverFile = new File(songCoverPath, coverName);

            if (!savedAudioFile.getParentFile().exists())
                savedAudioFile.getParentFile().mkdirs();
            if (!savedCoverFile.getParentFile().exists())
                savedCoverFile.getParentFile().mkdirs();

            // 3. 先保存原始文件
            audioFile.transferTo(savedAudioFile);
            if (coverFile != null && !coverFile.isEmpty()) {
                coverFile.transferTo(savedCoverFile);
            }

            // 4. 重点：将源信息写入文件内部 (ID3 Tags)
            AudioFile f = AudioFileIO.read(savedAudioFile);
            Tag tag = f.getTagOrCreateAndSetDefault(); // 如果没有标签就创建一个

            tag.setField(FieldKey.TITLE, finalTitle);
            tag.setField(FieldKey.ARTIST, finalArtist);
            tag.setField(FieldKey.ALBUM, finalAlbum);
            // 如果有歌词，也可以写进文件
            if (StringUtils.hasText(lyrics)) {
                tag.setField(FieldKey.LYRICS, lyrics);
            }

            // 提交修改到物理文件
            f.commit();

            // 5. 提取写入后的文件信息（时长、码率、MD5）
            AudioHeader header = f.getAudioHeader();
            int duration = header.getTrackLength();
            long bitrate = header.getBitRateAsNumber();

            // 计算指纹（建议在 commit 之后计算，因为 commit 改变了文件内容）
            String fileMd5 = calculateMD5(savedAudioFile);

            Users user = userMapper.selectById(uploaderId);
            if (user == null)
                throw new BusinessException(401, "上传失败：上传者身份无效");

            // 6. 保存到数据库
            Songs song = new Songs();
            song.setFileMd5(fileMd5); // 存入指纹
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

    // 辅助方法：计算MD5
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
}