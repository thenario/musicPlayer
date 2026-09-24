package com.kyf.mp.server.modules.song.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kyf.mp.server.modules.queue.entity.PlayHistory;
import com.kyf.mp.server.modules.queue.repository.PlayHistoryRepository;
import com.kyf.mp.server.modules.queue.service.workflow.QueuesWorkflow;
import com.kyf.mp.server.modules.song.dto.EditSongDTO;
import com.kyf.mp.server.modules.song.entity.Songs;
import com.kyf.mp.server.modules.song.repository.SongsRepository;
import com.kyf.mp.server.modules.song.service.SongsService;
import com.kyf.mp.server.modules.song.service.workflow.SongsWorkflow;
import com.kyf.mp.server.modules.song.vo.GetSongsVO;
import com.kyf.mp.server.modules.song.vo.LyricsVO;
import com.kyf.mp.server.modules.song.vo.PlayHistoryVO;
import com.kyf.mp.server.modules.song.vo.UploadsVO;

/**
 * <p>
 * 服务实现类：业务逻辑编排，业务流程委托给 SongsWorkflow。
 * </p>
 *
 * @author kyf
 * @since 2026-04-05
 */
@Service
public class SongsServiceImpl implements SongsService {

    private final SongsWorkflow songsWorkflow;
    private final SongsRepository songsRepository;
    private final PlayHistoryRepository playHistoryRepository;
    private final QueuesWorkflow queuesWorkflow;

    public SongsServiceImpl(SongsWorkflow songsWorkflow, SongsRepository songsRepository,
            PlayHistoryRepository playHistoryRepository, QueuesWorkflow queuesWorkflow) {
        this.songsWorkflow = songsWorkflow;
        this.songsRepository = songsRepository;
        this.playHistoryRepository = playHistoryRepository;
        this.queuesWorkflow = queuesWorkflow;
    }

    @Override
    public GetSongsVO getSongsPage(Integer page, String keyword) {
        return songsWorkflow.getSongsPage(page, keyword);
    }

    @Override
    @Cacheable(cacheNames = "song-lyrics", key = "#songId")
    public LyricsVO getLyrics(Long songId) {
        return songsWorkflow.getLyrics(songId);
    }

    @Override
    public void uploadSong(MultipartFile audioFile, MultipartFile coverFile, Long userId,
            String title, String artist, String album, String lyrics) {
        songsWorkflow.uploadSong(audioFile, coverFile, userId, title, artist, album, lyrics);
    }

    @Override
    public IPage<UploadsVO> getUploadSongs(Long userId, Integer page, Integer size) {
        // 查询条件封装在 Repository，Service 负责返回结构。
        Page<Songs> songPage = new Page<>(page, size);
        IPage<Songs> result = songsRepository.findUploads(userId, songPage);

        return result.convert(song -> {
            UploadsVO vo = new UploadsVO();
            BeanUtils.copyProperties(song, vo);
            return vo;
        });
    }

    @Override
    @CacheEvict(cacheNames = "song-lyrics", key = "#songID")
    public void editUploadSong(EditSongDTO dto, Long userId, Long songID) {
        songsWorkflow.editUploadSong(dto, userId, songID);
    }

    @Override
    public UploadsVO getUploadSong(Long userId, Long songId) {
        Songs song = songsRepository.findUpload(userId, songId);
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
        if (userId == null || songId == null) {
            throw new com.kyf.mp.server.common.BusinessException(400, "用户或歌曲不存在");
        }

        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        List<PlayHistory> sameSongHistories = playHistoryRepository.findByUserAndSongNewestFirst(userId, songId);

        if (sameSongHistories.isEmpty()) {
            PlayHistory playHistory = new PlayHistory();
            playHistory.setSongId(songId);
            playHistory.setUserId(userId);
            playHistory.setPlayedDate(now);
            playHistoryRepository.insert(playHistory);
        } else {
            // 同一首歌只保留一条记录，重复播放时更新这条记录的时间。
            PlayHistory playHistory = sameSongHistories.get(0);
            playHistory.setPlayedDate(now);
            playHistoryRepository.updateById(playHistory);

            List<Long> duplicateIds = sameSongHistories.stream()
                    .skip(1)
                    .map(PlayHistory::getHistoryId)
                    .toList();
            if (!duplicateIds.isEmpty()) {
                playHistoryRepository.deleteBatchIds(duplicateIds);
            }
        }

        List<PlayHistory> list = playHistoryRepository.findByUserOldestFirst(userId);
        if (list.size() > 100) {
            int deleteCounts = list.size() - 100;
            List<Long> deleteIds = list.stream().limit(deleteCounts).map(PlayHistory::getHistoryId).toList();
            playHistoryRepository.deleteBatchIds(deleteIds);
        }
    }

    @Override
    public List<PlayHistoryVO> getPlayHistory(Long userId) {
        if (userId == null) {
            throw new com.kyf.mp.server.common.BusinessException(404, "用户不存在");
        }

        return playHistoryRepository.selectHistoryWithSong(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void playAllhistory(Long userId) {
        if (userId == null) {
            throw new com.kyf.mp.server.common.BusinessException(404, "用户不存在");
        }

        List<PlayHistory> histories = playHistoryRepository.findByUserNewestFirst(userId);

        List<Long> songIds = histories.stream()
                .map(PlayHistory::getSongId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();

        if (songIds.isEmpty()) {
            throw new com.kyf.mp.server.common.BusinessException(404, "暂无播放历史");
        }

        queuesWorkflow.createQueueFromHistory(userId, songIds);
    }
}
