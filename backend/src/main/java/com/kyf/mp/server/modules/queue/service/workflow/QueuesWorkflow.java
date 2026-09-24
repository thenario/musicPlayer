package com.kyf.mp.server.modules.queue.service.workflow;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kyf.mp.server.common.BusinessException;
import com.kyf.mp.server.modules.playlist.entity.Playlists;
import com.kyf.mp.server.modules.playlist.entity.SongsPlaylistsRelation;
import com.kyf.mp.server.modules.playlist.repository.PlaylistsRepository;
import com.kyf.mp.server.modules.playlist.repository.SongsPlaylistsRelationRepository;
import com.kyf.mp.server.modules.queue.dto.AddSongToQueueDTO;
import com.kyf.mp.server.modules.queue.dto.UpdateCurrentQueueStateDTO;
import com.kyf.mp.server.modules.queue.entity.PlayState;
import com.kyf.mp.server.modules.queue.entity.QueueItems;
import com.kyf.mp.server.modules.queue.entity.Queues;
import com.kyf.mp.server.modules.queue.repository.PlayStateRepository;
import com.kyf.mp.server.modules.queue.repository.QueueCustomRepository;
import com.kyf.mp.server.modules.queue.repository.QueueItemsRepository;
import com.kyf.mp.server.modules.queue.repository.QueuesRepository;
import com.kyf.mp.server.modules.queue.vo.AddSongToQueueVO;
import com.kyf.mp.server.modules.queue.vo.AlterQueueVO;
import com.kyf.mp.server.modules.queue.vo.CreateQueueFromPlaylistVO;
import com.kyf.mp.server.modules.queue.vo.CurrentQueueVO;
import com.kyf.mp.server.modules.queue.vo.DeleteQueueVO;
import com.kyf.mp.server.modules.queue.vo.MyQueuesVO;
import com.kyf.mp.server.modules.queue.vo.ReturnQueueVO;
import com.kyf.mp.server.modules.queue.vo.SingleQueueVO;
import com.kyf.mp.server.modules.song.repository.SongsRepository;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** 业务流程：权限校验、事务编排、文件处理与结果组装；数据库操作委托给 Repository。 */
@Service
@Slf4j
@RequiredArgsConstructor
public class QueuesWorkflow {
    private final QueueCustomRepository queueCustomRepository;
    private final PlayStateRepository playStateRepository;
    private final QueueItemsRepository queueItemsRepository;
    private final QueuesRepository queuesRepository;
    private final PlaylistsRepository playlistsRepository;
    private final SongsPlaylistsRelationRepository songsPlaylistsRelationRepository;
    private final SongsRepository songsRepository;

    @Value("${queue.max-per-user:5}")
    private int maxQueuesPerUser;

    public CurrentQueueVO getCurrentQueue(Long userId) {
        if (userId == null) {
            throw new BusinessException(401, "用户未登录");
        }

        try {
            List<CurrentQueueVO> list = queueCustomRepository.selectCurrentQueueDetail(userId);

            if (list == null || list.isEmpty()) {
                return null;
            }

            CurrentQueueVO result = list.get(0);

            if (result == null || result.getQueueState() == null) {
                return null;
            }

            return result;

        } catch (Exception e) {
            log.error("获取当前队列 SQL 异常: ", e);
            throw new BusinessException(500, "获取失败");
        }
    }

    public MyQueuesVO getMyQueues(Long userId) {
        if (userId == null)
            throw new BusinessException(401, "用户未登录");

        List<ReturnQueueVO> list = queueCustomRepository.selectMyQueues(userId);

        MyQueuesVO result = new MyQueuesVO();
        result.setQueues(list != null ? list : new ArrayList<>());

        return result;
    }

    public SingleQueueVO getQueueById(Long userId, Long queueId) {
        ReturnQueueVO queueDetail = queueCustomRepository.selectQueueById(queueId, userId);

        if (queueDetail == null) {
            throw new BusinessException(404, "队列不存在");
        }

        SingleQueueVO vo = new SingleQueueVO();
        vo.setQueue(queueDetail);

        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public DeleteQueueVO deleteQueue(Long userId, Long queueId) {
        if (queueId == null || userId == null) {
            throw new BusinessException(400, "参数错误");
        }

        try {
            Queues queue = queuesRepository.findByOwner(queueId, userId);
            if (queue == null) {
                throw new BusinessException(404, "队列不存在或无权删除");
            }

            PlayState playState = playStateRepository.findByUser(userId);

            boolean isActive = playState != null && queueId.equals(playState.getCurrentQueueId());

            queueItemsRepository.deleteByQueue(queueId);

            int affectedRows = queuesRepository.deleteById(queueId);

            if (affectedRows == 0) {
                throw new BusinessException(500, "删除队列失败");
            }

            Long newQueueId = null;

            if (isActive) {
                Queues latestQueue = queuesRepository.findLatestUpdated(userId);

                newQueueId = (latestQueue != null) ? latestQueue.getQueueId() : null;

                playState.setCurrentQueueId(newQueueId);
                playState.setCurrentSongId(null);
                playState.setCurrentPosition(0);
                playState.setCurrentProgress(0);
                playStateRepository.updateById(playState);
            }

            DeleteQueueVO vo = new DeleteQueueVO();
            vo.setWasActive(isActive);
            vo.setNewQueueId(newQueueId);

            return vo;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除队列异常: ", e);
            throw new BusinessException(500, "删除队列失败");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void clearQueue(Long userId, Long queueId) {
        if (userId == null || queueId == null) {
            log.error("clearQueue 收到空参数: userId={}, queueId={}", userId, queueId);
            throw new BusinessException(400, "参数异常，清空失败");
        }

        Queues queue = queuesRepository.findByOwner(queueId, userId);

        if (queue == null) {
            throw new BusinessException(403, "无权操作该队列或队列已不存在");
        }

        try {
            queueItemsRepository.deleteByQueue(queueId);

            queue.setSongCount(0);
            queue.setUpdatedDate(LocalDateTime.now(ZoneId.systemDefault()));
            queuesRepository.updateById(queue);

            PlayState playState = playStateRepository.findByUserAndQueue(userId, queueId);

            if (playState != null) {
                playState.setCurrentSongId(null);
                playState.setCurrentPosition(0);
                playState.setCurrentProgress(0);
                playStateRepository.updateById(playState);
            }

        } catch (Exception e) {
            log.error("清空队列失败，queueId: {}, 原因: ", queueId, e);
            throw new BusinessException(500, "系统错误，清空队列失败");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public CreateQueueFromPlaylistVO createQueueFromPlaylist(Long userId, Long playlistId) {
        if (userId == null || playlistId == null || playlistId <= 0) {
            throw new BusinessException(400, "参数错误，歌单ID不能为空");
        }

        try {
            PlayState playState = playStateRepository.findByUser(userId);
            List<Queues> existingQueues = queuesRepository.findByCreatorOldestFirst(userId);

            if (existingQueues != null && existingQueues.size() >= maxQueuesPerUser) {
                Queues removableQueue = existingQueues.stream()
                        .filter(queue -> playState == null
                                || !Objects.equals(queue.getQueueId(), playState.getCurrentQueueId()))
                        .findFirst()
                        .orElseThrow(() -> new BusinessException(409, "当前播放队列不可自动删除"));
                queueItemsRepository.deleteByQueue(removableQueue.getQueueId());
                queuesRepository.deleteById(removableQueue.getQueueId());
            }
            Playlists playlist = playlistsRepository.getById(playlistId);
            if (playlist == null) {
                throw new BusinessException(404, "歌单不存在");
            }
            if (!Objects.equals(playlist.getCreatorId(), userId) && !Boolean.TRUE.equals(playlist.getPubliclyVisible())) {
                throw new BusinessException(403, "无权访问此私密歌单");
            }
            String queueName = playlist.getPlaylistName();

            Queues newQueue = new Queues();
            newQueue.setQueueName(queueName);
            newQueue.setCreatorId(userId);
            newQueue.setCurrent(true);
            newQueue.setSongCount(0);
            queuesRepository.insert(newQueue);
            Long newQueueId = newQueue.getQueueId();

            List<SongsPlaylistsRelation> relations = songsPlaylistsRelationRepository.findByPlaylistId(playlistId);
            for (SongsPlaylistsRelation relation : relations) {
                QueueItems queueItem = new QueueItems();
                queueItem.setQueueId(newQueueId);
                queueItem.setSongId(relation.getSongId());
                queueItem.setQueueItemPosition(relation.getSongPlaylistPosition());
                queueItemsRepository.insert(queueItem);
            }
            int insertedSongs = relations.size();

            newQueue.setSongCount(insertedSongs);
            queuesRepository.updateById(newQueue);

            if (playState != null) {
                playState.setCurrentQueueId(newQueueId);
                playState.setCurrentSongId(null);
                playState.setCurrentProgress(0);
                playStateRepository.updateById(playState);
            }

            CreateQueueFromPlaylistVO vo = new CreateQueueFromPlaylistVO();
            vo.setQueueId(newQueueId);
            vo.setSongCount(insertedSongs);

            return vo;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("从歌单创建队列失败", e);
            throw new BusinessException(500, "创建队列失败");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public Long createQueueFromHistory(Long userId, List<Long> songIds) {
        if (userId == null || songIds == null || songIds.isEmpty()) {
            throw new BusinessException(400, "播放历史为空");
        }

        PlayState playState = playStateRepository.findByUser(userId);
        boolean hasPlayState = playState != null;
        Long currentQueueId = hasPlayState ? playState.getCurrentQueueId() : null;

        List<Queues> existingQueues = queuesRepository.findByCreatorOldestFirst(userId);

        if (existingQueues.size() >= maxQueuesPerUser) {
            Queues removableQueue = existingQueues.stream()
                    .filter(queue -> !Objects.equals(queue.getQueueId(), currentQueueId))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException(409, "当前播放队列不可自动删除"));

            queueItemsRepository.deleteByQueue(removableQueue.getQueueId());
            queuesRepository.deleteById(removableQueue.getQueueId());
        }

        // 新队列设为当前队列前，取消该用户其他队列的当前标记。
        queuesRepository.clearCurrentFlags(userId);

        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        Queues queue = new Queues();
        queue.setQueueName("播放历史");
        queue.setCreatorId(userId);
        queue.setCurrent(true);
        queue.setSongCount(songIds.size());
        queue.setCreatedDate(now);
        queue.setUpdatedDate(now);
        queuesRepository.insert(queue);

        for (int i = 0; i < songIds.size(); i++) {
            QueueItems item = new QueueItems();
            item.setQueueId(queue.getQueueId());
            item.setSongId(songIds.get(i));
            item.setQueueItemPosition(i + 1);
            item.setAddedDate(now);
            queueItemsRepository.insert(item);
        }

        if (!hasPlayState) {
            playState = new PlayState();
            playState.setUserId(userId);
            playState.setPlaymode("sequential");
            playState.setCurrentProgress(0);
        }
        playState.setCurrentQueueId(queue.getQueueId());
        playState.setCurrentSongId(songIds.get(0));
        playState.setCurrentPosition(1);
        playState.setCurrentProgress(0);
        playState.setUpdatedDate(now);

        if (hasPlayState) {
            playStateRepository.updateById(playState);
        } else {
            playStateRepository.insert(playState);
        }

        return queue.getQueueId();
    }

    @Transactional(rollbackFor = Exception.class)
    public AddSongToQueueVO addSongToQueue(Long userId, Long paramQueueId, AddSongToQueueDTO dto) {
        Long songId = dto.getSongId();
        boolean mode = dto.getMode() != null && dto.getMode();

        if (songsRepository.getById(songId) == null) {
            throw new BusinessException(404, "歌曲不存在");
        }

        QueueContext context = ensureQueueId(userId, paramQueueId);
        Long finalQueueId = context.getFinalQueueId();

        PlayState playState = playStateRepository.findByUser(userId);

        int currentPos = (playState != null) ? playState.getCurrentPosition() : 0;
        boolean hasState = (playState != null);

        int insertPos = Math.max(1, currentPos + 1);

        QueueItems existingItem = queueItemsRepository.findByQueueAndSong(finalQueueId, songId);
        boolean wasExisted = existingItem != null;
        if (wasExisted) {
            queueItemsRepository.deleteById(existingItem.getQueueItemId());
            queueCustomRepository.shiftPositionsDown(finalQueueId, existingItem.getQueueItemPosition());
            if (existingItem.getQueueItemPosition() < insertPos) {
                insertPos--;
            }
        }

        queueCustomRepository.moveItemPositionsToTemporary(finalQueueId, insertPos);

        QueueItems newItem = new QueueItems();
        newItem.setQueueId(finalQueueId);
        newItem.setSongId(songId);
        newItem.setQueueItemPosition(insertPos);
        newItem.setAddedDate(LocalDateTime.now(ZoneId.systemDefault()));
        queueItemsRepository.insert(newItem);
        queueCustomRepository.restoreShiftedItemPositions(finalQueueId, insertPos);

        // mode=true 表示立即播放；下一首播放仅调整队列顺序。
        if (mode || !hasState || context.isNewQueue()) {
            if (playState == null) {
                playState = new PlayState();
                playState.setUserId(userId);
            }
            playState.setCurrentQueueId(finalQueueId);
            playState.setCurrentSongId(songId);
            playState.setCurrentPosition(insertPos);
            playState.setCurrentProgress(0);
            playState.setUpdatedDate(LocalDateTime.now(ZoneId.systemDefault()));

            if (!hasState)
                playStateRepository.insert(playState);
            else
                playStateRepository.updateById(playState);
        }

        // 删除并重插已有歌曲时，当前歌曲的位置可能随队列移位；以实际队列项为准。
        queueCustomRepository.syncPlayStatePosition(userId, finalQueueId);

        if (!wasExisted) {
            queueCustomRepository.incrementSongCount(finalQueueId);
        }

        AddSongToQueueVO vo = new AddSongToQueueVO();
        vo.setQueueId(finalQueueId);
        vo.setQueueItemPosition(insertPos);
        vo.setQueueItemId(newItem.getQueueItemId());

        return vo;
    }

    private QueueContext ensureQueueId(Long userId, Long qId) {
        if (qId != null && qId > 0) {
            Queues queue = queuesRepository.findByOwner(qId, userId);
            if (queue == null) {
                throw new BusinessException(404, "队列不存在或无权操作");
            }
            return new QueueContext(qId, false);
        }

        Queues latest = queuesRepository.findLatestCreated(userId);

        if (latest != null) {
            return new QueueContext(latest.getQueueId(), false);
        }

        Long count = queuesRepository.countByCreator(userId);
        if (count >= maxQueuesPerUser) {
            Queues oldest = queuesRepository.findOldestCreated(userId);
            queueItemsRepository.deleteByQueue(oldest.getQueueId());
            queuesRepository.deleteById(oldest.getQueueId());
        }

        Queues newQ = new Queues();
        newQ.setQueueName("默认列表");
        newQ.setCreatorId(userId);
        newQ.setSongCount(0);
        newQ.setCreatedDate(LocalDateTime.now(ZoneId.systemDefault()));
        queuesRepository.insert(newQ);

        return new QueueContext(newQ.getQueueId(), true);
    }

    @Data
    @AllArgsConstructor
    private static class QueueContext {
        private Long finalQueueId;
        private boolean newQueue;
    }

    @Transactional(rollbackFor = Exception.class)
    public void removeSongFromQueue(Long userId, Long queueId, Long queueItemId) {
        Map<String, Object> itemInfo = queueCustomRepository.selectItemDetailForDelete(queueItemId, userId);

        if (itemInfo == null || itemInfo.isEmpty()) {
            throw new BusinessException(404, "未找到该歌曲或无权操作");
        }

        Long itemQueueId = (Long) itemInfo.get("queue_id");
        if (!queueId.equals(itemQueueId)) {
            throw new BusinessException(404, "队列中未找到该歌曲");
        }
        Integer removedPos = (Integer) itemInfo.get("queue_item_position");
        Long removedSongId = (Long) itemInfo.get("song_id");

        queueItemsRepository.deleteById(queueItemId);
        queueCustomRepository.shiftPositionsDown(itemQueueId, removedPos);

        PlayState playState = playStateRepository.findByUserAndQueue(userId, itemQueueId);

        if (playState != null && removedSongId.equals(playState.getCurrentSongId())) {
            QueueItems nextItem = queueItemsRepository.findByQueueAndPosition(itemQueueId, removedPos);

            Long nextSongId = (nextItem != null) ? nextItem.getSongId() : null;
            playState.setCurrentSongId(nextSongId);
            playState.setCurrentPosition(nextItem != null ? nextItem.getQueueItemPosition() : 0);
            playState.setCurrentProgress(0);
            playStateRepository.updateById(playState);
        }
        // 删除当前歌曲之前的项目会使其位置前移，按当前歌曲重新同步实际位置。
        queueCustomRepository.syncPlayStatePosition(userId, itemQueueId);
        queueCustomRepository.decrementSongCount(itemQueueId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateCurrentQueueState(Long userId, UpdateCurrentQueueStateDTO dto) {

        Queues queue = queuesRepository.findByOwner(dto.getCurrentQueueId(), userId);
        if (queue == null) {
            throw new BusinessException(404, "队列不存在或无权操作");
        }

        Integer finalPosition = 0;
        if (dto.getCurrentSongId() != null) {
            QueueItems item = queueItemsRepository.findFirstByQueueAndSong(dto.getCurrentQueueId(), dto.getCurrentSongId());
            if (item == null) {
                throw new BusinessException(400, "当前歌曲不在该队列中");
            }
            finalPosition = item.getQueueItemPosition();
        }

        PlayState playState = playStateRepository.findByUser(userId);

        if (playState == null) {
            playState = new PlayState();
            playState.setUserId(userId);
            mapDtoToEntity(playState, dto, finalPosition);
            playStateRepository.insert(playState);
        } else {
            mapDtoToEntity(playState, dto, finalPosition);
            playStateRepository.updateById(playState);
        }

    }

    private void mapDtoToEntity(PlayState ps, UpdateCurrentQueueStateDTO dto, Integer pos) {
        ps.setCurrentQueueId(dto.getCurrentQueueId());
        ps.setCurrentSongId(dto.getCurrentSongId());
        ps.setCurrentPosition(pos);

        ps.setCurrentProgress(dto.getCurrentProgress() != null ? dto.getCurrentProgress() : 0);
        ps.setPlaymode(dto.getPlaymode() != null ? dto.getPlaymode() : "sequential");

        ps.setUpdatedDate(LocalDateTime.now(ZoneId.systemDefault()));
    }

    @Transactional(rollbackFor = Exception.class)
    public AlterQueueVO alterQueueToCurrent(Long userId, Long queueId) {
        Queues queue = queuesRepository.findByOwner(queueId, userId);

        if (queue == null) {
            throw new BusinessException(403, "队列不存在或无权访问");
        }

        queuesRepository.clearOtherCurrentFlags(userId, queueId);
        queuesRepository.setCurrentFlag(userId, queueId);

        QueueItems firstItem = queueItemsRepository.findByQueueAndPosition(queueId, 1);

        Long firstSongId = (firstItem != null) ? firstItem.getSongId() : null;

        PlayState playState = playStateRepository.findByUser(userId);

        if (playState == null) {
            playState = new PlayState();
            playState.setUserId(userId);
            fillPlayState(playState, queueId, firstSongId);
            playStateRepository.insert(playState);
        } else {
            fillPlayState(playState, queueId, firstSongId);
            playStateRepository.updateById(playState);
        }

        AlterQueueVO vo = new AlterQueueVO();
        vo.setCurrentSongId(firstSongId);
        vo.setCurrentPosition(1);

        return vo;
    }

    private void fillPlayState(PlayState ps, Long queueId, Long songId) {
        ps.setCurrentQueueId(queueId);
        ps.setCurrentSongId(songId);
        ps.setCurrentPosition(1);
        ps.setCurrentProgress(0);
        ps.setUpdatedDate(LocalDateTime.now(ZoneId.systemDefault()));
    }

    @Transactional(rollbackFor = Exception.class)
    public void setPlayMode(Long userId, Long queueId, String playMode) {
        Queues queue = queuesRepository.findByOwner(queueId, userId);

        if (queue == null) {
            throw new BusinessException(403, "无权操作此队列");
        }

        PlayState playState = playStateRepository.findByUser(userId);

        if (playState == null) {
            playState = new PlayState();
            playState.setUserId(userId);
            playState.setCurrentQueueId(queueId);
            playState.setPlaymode(playMode);
            playState.setUpdatedDate(LocalDateTime.now(ZoneId.systemDefault()));
            playStateRepository.insert(playState);
        } else {
            playState.setCurrentQueueId(queueId);
            playState.setPlaymode(playMode);
            playState.setUpdatedDate(LocalDateTime.now(ZoneId.systemDefault()));
            playStateRepository.updateById(playState);
        }

    }

    @Transactional(rollbackFor = Exception.class)
    public void reorderQueue(Long userId, Long queueId, List<Long> songIds) {
        Queues queue = queuesRepository.findByOwner(queueId, userId);

        if (queue == null) {
            throw new BusinessException(403, "无权操作此队列");
        }

        List<QueueItems> queueItems = queueItemsRepository.findByQueue(queueId);
        Set<Long> requestedSongIds = new HashSet<>(songIds);
        Set<Long> queueSongIds = new HashSet<>();
        for (QueueItems item : queueItems) {
            queueSongIds.add(item.getSongId());
        }
        if (requestedSongIds.size() != songIds.size() || !requestedSongIds.equals(queueSongIds)) {
            throw new BusinessException(400, "重排歌曲列表必须与队列内容一致");
        }

        queueCustomRepository.moveAllItemPositionsToTemporary(queueId);
        queueCustomRepository.batchUpdatePositions(queueId, songIds);

        queueCustomRepository.syncPlayStatePosition(userId, queueId);
    }
}
