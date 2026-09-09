package com.kyf.mp.server.modules.song.service.impl;

import com.kyf.mp.server.common.auth.LoginRateLimiter;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kyf.mp.server.modules.queue.entity.PlayHistory;
import com.kyf.mp.server.modules.queue.business.QueuesBusiness;
import com.kyf.mp.server.modules.song.business.SongsBusiness;
import com.kyf.mp.server.modules.song.dto.EditSongDTO;
import com.kyf.mp.server.modules.song.entity.Songs;
import com.kyf.mp.server.modules.song.mapper.PlayHistoryMapper;
import com.kyf.mp.server.modules.song.service.SongsService;
import com.kyf.mp.server.modules.song.vo.GetSongsVO;
import com.kyf.mp.server.modules.song.vo.LyricsVO;
import com.kyf.mp.server.modules.song.vo.PlayHistoryVO;
import com.kyf.mp.server.modules.song.vo.UploadsVO;

import lombok.RequiredArgsConstructor;

/**
 * <p>
 * 服务实现类：业务逻辑编排，数据访问委托给 SongsBusiness。
 * </p>
 *
 * @author kyf
 * @since 2026-04-05
 */
@Service
public class SongsServiceImpl implements SongsService {

    private final LoginRateLimiter loginRateLimiter;
    private final SongsBusiness songsBusiness;
    private final PlayHistoryMapper playHistoryMapper;
    private final QueuesBusiness queuesBusiness;

    SongsServiceImpl(LoginRateLimiter loginRateLimiter, PlayHistoryMapper playHistoryMapper,
            SongsBusiness songsBusiness, QueuesBusiness queuesBusiness) {
        this.loginRateLimiter = loginRateLimiter;
        this.playHistoryMapper = playHistoryMapper;
        this.songsBusiness = songsBusiness;
        this.queuesBusiness = queuesBusiness;
    }

    @Override
    public GetSongsVO getSongsPage(Integer page, String keyword) {
        return songsBusiness.getSongsPage(page, keyword);
    }

    @Override
    @Cacheable(cacheNames = "song-lyrics", key = "#songId")
    public LyricsVO getLyrics(Long songId) {
        return songsBusiness.getLyrics(songId);
    }

    @Override
    public void uploadSong(MultipartFile audioFile, MultipartFile coverFile, Long userId,
            String title, String artist, String album, String lyrics) {
        songsBusiness.uploadSong(audioFile, coverFile, userId, title, artist, album, lyrics);
    }

    @Override
    public IPage<UploadsVO> getUploadSongs(Long userId, Integer page, Integer size) {
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
    @CacheEvict(cacheNames = "song-lyrics", key = "#songID")
    public void editUploadSong(EditSongDTO dto, Long userId, Long songID) {
        songsBusiness.editUploadSong(dto, userId, songID);
    }

    @Override
    public UploadsVO getUploadSong(Long userId, Long songId) {
        Songs song = songsBusiness.getOne(new LambdaQueryWrapper<Songs>()
                .eq(Songs::getSongId, songId)
                .eq(Songs::getUploaderId, userId));
        if (song == null) {
            throw new com.kyf.mp.server.common.BusinessException(404, "上传歌曲不存在");
        }
        UploadsVO vo = new UploadsVO();
        BeanUtils.copyProperties(song, vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncPlayHistory(Long userId, Long songId) {
        if (userId == null) {
            throw new com.kyf.mp.server.common.BusinessException(404, "用户不存在");
        }
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", userId);
        params.put("song_id", songId);
        List<PlayHistory> histories = playHistoryMapper.selectByMap(params);
        if (!histories.isEmpty()) {
            PlayHistory playHistory = histories.get(0);
            playHistory.setPlayedDate(LocalDateTime.now(ZoneId.systemDefault()));
            playHistoryMapper.updateById(playHistory);

        }
        PlayHistory playHistory = new PlayHistory();
        playHistory.setSongId(songId);
        playHistory.setUserId(userId);
        playHistory.setPlayedDate(
                LocalDateTime.now(ZoneId.systemDefault()));
        playHistoryMapper.insert(playHistory);

        LambdaQueryWrapper<PlayHistory> wrapper = new LambdaQueryWrapper<PlayHistory>()
                .eq(PlayHistory::getUserId, userId)
                .orderByAsc(PlayHistory::getPlayedDate)
                .orderByAsc(PlayHistory::getHistoryId);

        List<PlayHistory> list = playHistoryMapper.selectList(wrapper);
        if (list.size() > 100) {
            int deleteCounts = list.size() - 100;
            List<Long> deleteIds = list.stream().limit(deleteCounts).map(PlayHistory::getHistoryId).toList();
            playHistoryMapper.deleteBatchIds(deleteIds);
        }
    }

    @Override
    public List<PlayHistoryVO> getPlayHistory(Long userId) {
        if (userId == null) {
            throw new com.kyf.mp.server.common.BusinessException(404, "用户不存在");
        }

        return playHistoryMapper.selectHistoryWithSong(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void playAllhistory(Long userId) {
        if (userId == null) {
            throw new com.kyf.mp.server.common.BusinessException(404, "用户不存在");
        }

        List<PlayHistory> histories = playHistoryMapper.selectList(new LambdaQueryWrapper<PlayHistory>()
                .eq(PlayHistory::getUserId, userId)
                .orderByDesc(PlayHistory::getPlayedDate)
                .orderByDesc(PlayHistory::getHistoryId));

        List<Long> songIds = histories.stream()
                .map(PlayHistory::getSongId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();

        if (songIds.isEmpty()) {
            throw new com.kyf.mp.server.common.BusinessException(404, "暂无播放历史");
        }

        queuesBusiness.createQueueFromHistory(userId, songIds);
    }
}
