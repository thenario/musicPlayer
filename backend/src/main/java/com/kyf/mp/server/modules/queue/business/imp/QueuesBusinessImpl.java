package com.kyf.mp.server.modules.queue.business.imp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kyf.mp.server.common.BusinessException;
import com.kyf.mp.server.common.business.BaseBusinessImpl;
import com.kyf.mp.server.modules.playlist.entity.Playlists;
import com.kyf.mp.server.modules.playlist.mapper.PlaylistsMapper;
import com.kyf.mp.server.modules.queue.business.QueuesBusiness;
import com.kyf.mp.server.modules.queue.dto.AddSongToQueue;
import com.kyf.mp.server.modules.queue.dto.UpdateCurrentQueueStateDTO;
import com.kyf.mp.server.modules.queue.entity.PlayState;
import com.kyf.mp.server.modules.queue.entity.QueueItems;
import com.kyf.mp.server.modules.queue.entity.Queues;
import com.kyf.mp.server.modules.queue.mapper.PlayStateMapper;
import com.kyf.mp.server.modules.queue.mapper.QueueCustomMapper;
import com.kyf.mp.server.modules.queue.mapper.QueueItemsMapper;
import com.kyf.mp.server.modules.queue.mapper.QueuesMapper;
import com.kyf.mp.server.modules.queue.vo.AddSongToQueueVO;
import com.kyf.mp.server.modules.queue.vo.AlterQueueVO;
import com.kyf.mp.server.modules.queue.vo.CreateQueueFromPlaylist;
import com.kyf.mp.server.modules.queue.vo.CurrentQueue;
import com.kyf.mp.server.modules.queue.vo.DeleteQueueVO;
import com.kyf.mp.server.modules.queue.vo.MyQueues;
import com.kyf.mp.server.modules.queue.vo.ReturnQueue;
import com.kyf.mp.server.modules.queue.vo.SingleQueue;
import com.kyf.mp.server.modules.song.mapper.SongsMapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 队列数据访问实现：复杂数据库操作。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class QueuesBusinessImpl extends BaseBusinessImpl<QueuesMapper, Queues> implements QueuesBusiness {
    private final QueueCustomMapper queueCustomMapper;
    private final PlayStateMapper playStateMapper;
    private final QueueItemsMapper queueItemsMapper;
    private final QueuesMapper queuesMapper;
    private final PlaylistsMapper playlistsMapper;
    private final SongsMapper songsMapper;

    @Override
    public CurrentQueue getCurrentQueue(Long userId) {
        if (userId == null) {
            throw new BusinessException(401, "用户未登录");
        }

        try {
            List<CurrentQueue> list = queueCustomMapper.selectCurrentQueueDetail(userId);

            if (list == null || list.isEmpty()) {
                return null;
            }

            CurrentQueue result = list.get(0);

            if (result == null || result.getQueueState() == null) {
                return null;
            }

            return result;

        } catch (Exception e) {
            log.error("获取当前队列 SQL 异常: ", e);
            throw new BusinessException(500, "获取失败");
        }
    }

    @Override
    public MyQueues getMyQueues(Long userId) {
        if (userId == null)
            throw new BusinessException(401, "用户未登录");

        List<ReturnQueue> list = queueCustomMapper.selectMyQueues(userId);

        MyQueues result = new MyQueues();
        result.setQueues(list != null ? list : new ArrayList<>());

        return result;
    }

    @Override
    public SingleQueue getQueueById(Long userId, Long queueId) {
        ReturnQueue queueDetail = queueCustomMapper.selectQueueById(queueId, userId);

        if (queueDetail == null) {
            throw new BusinessException(404, "队列不存在");
        }

        SingleQueue vo = new SingleQueue();
        vo.setQueue(queueDetail);

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeleteQueueVO deleteQueue(Long userId, Long queueId) {
        if (queueId == null || userId == null) {
            throw new BusinessException(400, "参数错误");
        }

        try {
            Queues queue = queuesMapper.selectOne(new LambdaQueryWrapper<Queues>()
                    .eq(Queues::getQueueId, queueId)
                    .eq(Queues::getCreatorId, userId));
            if (queue == null) {
                throw new BusinessException(404, "队列不存在或无权删除");
            }

            PlayState playState = playStateMapper.selectOne(
                    new LambdaQueryWrapper<PlayState>().eq(PlayState::getUserId, userId));

            boolean isActive = playState != null && queueId.equals(playState.getCurrentQueueId());

            queueItemsMapper.delete(
                    new LambdaQueryWrapper<QueueItems>().eq(QueueItems::getQueueId, queueId));

            int affectedRows = queuesMapper.deleteById(queueId);

            if (affectedRows == 0) {
                throw new BusinessException(500, "删除队列失败");
            }

            Long newQueueId = null;

            if (isActive) {
                Queues latestQueue = queuesMapper.selectOne(
                        new LambdaQueryWrapper<Queues>()
                                .eq(Queues::getCreatorId, userId)
                                .orderByDesc(Queues::getUpdatedDate)
                                .last("LIMIT 1"));

                newQueueId = (latestQueue != null) ? latestQueue.getQueueId() : null;

                playState.setCurrentQueueId(newQueueId);
                playState.setCurrentSongId(null);
                playState.setCurrentPosition(0);
                playState.setCurrentProgress(0);
                playStateMapper.updateById(playState);
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearQueue(Long userId, Long queueId) {
        if (userId == null || queueId == null) {
            log.error("clearQueue 收到空参数: userId={}, queueId={}", userId, queueId);
            throw new BusinessException(400, "参数异常，清空失败");
        }

        Queues queue = queuesMapper.selectOne(new LambdaQueryWrapper<Queues>()
                .eq(Queues::getQueueId, queueId)
                .eq(Queues::getCreatorId, userId));

        if (queue == null) {
            throw new BusinessException(403, "无权操作该队列或队列已不存在");
        }

        try {
            queueItemsMapper.delete(new LambdaQueryWrapper<QueueItems>()
                    .eq(QueueItems::getQueueId, queueId));

            queue.setSongCount(0);
            queue.setUpdatedDate(LocalDateTime.now());
            queuesMapper.updateById(queue);

            PlayState playState = playStateMapper.selectOne(new LambdaQueryWrapper<PlayState>()
                    .eq(PlayState::getUserId, userId)
                    .eq(PlayState::getCurrentQueueId, queueId));

            if (playState != null) {
                playState.setCurrentSongId(null);
                playState.setCurrentPosition(0);
                playState.setCurrentProgress(0);
                playStateMapper.updateById(playState);
            }

        } catch (Exception e) {
            log.error("清空队列失败，queueId: {}, 原因: ", queueId, e);
            throw new BusinessException(500, "系统错误，清空队列失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CreateQueueFromPlaylist createQueueFromPlaylist(Long userId, Long playlistId) {
        if (userId == null || playlistId == null || playlistId <= 0) {
            throw new BusinessException(400, "参数错误，歌单ID不能为空");
        }

        try {
            List<Queues> existingQueues = queuesMapper.selectList(new LambdaQueryWrapper<Queues>()
                    .eq(Queues::getCreatorId, userId)
                    .orderByAsc(Queues::getCreatedDate));

            if (existingQueues != null && existingQueues.size() >= 5) {
                Long oldestId = existingQueues.get(0).getQueueId();
                queueItemsMapper.delete(new LambdaQueryWrapper<QueueItems>().eq(QueueItems::getQueueId, oldestId));
                queuesMapper.deleteById(oldestId);
            }

            Playlists playlist = playlistsMapper.selectById(playlistId);
            if (playlist == null) {
                throw new BusinessException(404, "歌单不存在");
            }`r`n            if (!Objects.equals(playlist.getCreatorId(), userId) && !Boolean.TRUE.equals(playlist.getIsPublic())) {`r`n                throw new BusinessException(403, "无权访问此私密歌单");`r`n            }
            String queueName = playlist.getPlaylistName();

            Queues newQueue = new Queues();
            newQueue.setQueueName(queueName);
            newQueue.setCreatorId(userId);
            newQueue.setIsCurrent(true);
            newQueue.setSongCount(0);
            queuesMapper.insert(newQueue);
            Long newQueueId = newQueue.getQueueId();

            int insertedSongs = queueCustomMapper.copySongsFromPlaylistToQueue(newQueueId, playlistId);

            newQueue.setSongCount(insertedSongs);
            queuesMapper.updateById(newQueue);

            PlayState playState = playStateMapper.selectOne(new LambdaQueryWrapper<PlayState>()
                    .eq(PlayState::getUserId, userId));

            if (playState != null) {
                playState.setCurrentQueueId(newQueueId);
                playState.setCurrentSongId(null);
                playState.setCurrentProgress(0);
                playStateMapper.updateById(playState);
            }

            CreateQueueFromPlaylist vo = new CreateQueueFromPlaylist();
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AddSongToQueueVO addSongToQueue(Long userId, Long paramQueueId, AddSongToQueue dto) {
        Long songId = dto.getSongId();
        boolean mode = dto.getMode() != null && dto.getMode();

        if (songsMapper.selectById(songId) == null) {
            throw new BusinessException(404, "歌曲不存在");
        }

        QueueContext context = ensureQueueId(userId, paramQueueId);
        Long finalQueueId = context.getFinalQueueId();

        PlayState playState = playStateMapper.selectOne(new LambdaQueryWrapper<PlayState>()
                .eq(PlayState::getUserId, userId));

        int currentPos = (playState != null) ? playState.getCurrentPosition() : 0;
        boolean hasState = (playState != null);

        int insertPos = Math.max(1, mode ? currentPos + 1 : currentPos);

        int deleted = queueItemsMapper.delete(new LambdaQueryWrapper<QueueItems>()
                .eq(QueueItems::getQueueId, finalQueueId)
                .eq(QueueItems::getSongId, songId));
        boolean wasExisted = deleted > 0;

        queueCustomMapper.moveItemPositionsToTemporary(finalQueueId, insertPos);

        QueueItems newItem = new QueueItems();
        newItem.setQueueId(finalQueueId);
        newItem.setSongId(songId);
        newItem.setQueueItemPosition(insertPos);
        newItem.setAddedDate(LocalDateTime.now());
        queueItemsMapper.insert(newItem);
        queueCustomMapper.restoreShiftedItemPositions(finalQueueId, insertPos);

        if (!mode || !hasState || context.isNewQueue()) {
            if (playState == null) {
                playState = new PlayState();
                playState.setUserId(userId);
            }
            playState.setCurrentQueueId(finalQueueId);
            playState.setCurrentSongId(songId);
            playState.setCurrentPosition(insertPos);
            playState.setCurrentProgress(0);
            playState.setUpdatedDate(LocalDateTime.now());

            if (!hasState)
                playStateMapper.insert(playState);
            else
                playStateMapper.updateById(playState);
        }

        if (!wasExisted) {
            queueCustomMapper.incrementSongCount(finalQueueId);
        }

        AddSongToQueueVO vo = new AddSongToQueueVO();
        vo.setQueueId(finalQueueId);
        vo.setQueueItemPosition(insertPos);
        vo.setQueueItemId(newItem.getQueueItemId());

        return vo;
    }

    private QueueContext ensureQueueId(Long userId, Long qId) {
        if (qId != null && qId > 0) {
            Queues queue = queuesMapper.selectOne(new LambdaQueryWrapper<Queues>()
                    .eq(Queues::getQueueId, qId)
                    .eq(Queues::getCreatorId, userId));
            if (queue == null) {
                throw new BusinessException(404, "队列不存在或无权操作");
            }
            return new QueueContext(qId, false);
        }

        Queues latest = queuesMapper.selectOne(new LambdaQueryWrapper<Queues>()
                .eq(Queues::getCreatorId, userId)
                .orderByDesc(Queues::getCreatedDate)
                .last("LIMIT 1"));

        if (latest != null) {
            return new QueueContext(latest.getQueueId(), false);
        }

        Long count = queuesMapper.selectCount(new LambdaQueryWrapper<Queues>().eq(Queues::getCreatorId, userId));
        if (count >= 5) {
            Queues oldest = queuesMapper.selectOne(new LambdaQueryWrapper<Queues>()
                    .eq(Queues::getCreatorId, userId)
                    .orderByAsc(Queues::getCreatedDate)
                    .last("LIMIT 1"));
            queueItemsMapper
                    .delete(new LambdaQueryWrapper<QueueItems>().eq(QueueItems::getQueueId, oldest.getQueueId()));
            queuesMapper.deleteById(oldest.getQueueId());
        }

        Queues newQ = new Queues();
        newQ.setQueueName("默认列表");
        newQ.setCreatorId(userId);
        newQ.setSongCount(0);
        newQ.setCreatedDate(LocalDateTime.now());
        queuesMapper.insert(newQ);

        return new QueueContext(newQ.getQueueId(), true);
    }

    @Data
    @AllArgsConstructor
    private static class QueueContext {
        private Long finalQueueId;
        private boolean isNewQueue;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeSongFromQueue(Long userId, Long queueId, Long queueItemId) {
        Map<String, Object> itemInfo = queueCustomMapper.selectItemDetailForDelete(queueItemId, userId);

        if (itemInfo == null || itemInfo.isEmpty()) {
            throw new BusinessException(404, "未找到该歌曲或无权操作");
        }

        Long itemQueueId = (Long) itemInfo.get("queue_id");
        if (!queueId.equals(itemQueueId)) {
            throw new BusinessException(404, "队列中未找到该歌曲");
        }
        Integer removedPos = (Integer) itemInfo.get("queue_item_position");
        Long removedSongId = (Long) itemInfo.get("song_id");

        queueItemsMapper.deleteById(queueItemId);
        queueCustomMapper.shiftPositionsDown(itemQueueId, removedPos);

        PlayState playState = playStateMapper.selectOne(new LambdaQueryWrapper<PlayState>()
                .eq(PlayState::getUserId, userId)
                .eq(PlayState::getCurrentQueueId, itemQueueId));

        if (playState != null && removedSongId.equals(playState.getCurrentSongId())) {
            QueueItems nextItem = queueItemsMapper.selectOne(new LambdaQueryWrapper<QueueItems>()
                    .eq(QueueItems::getQueueId, itemQueueId)
                    .eq(QueueItems::getQueueItemPosition, removedPos)
                    .last("LIMIT 1"));

            Long nextSongId = (nextItem != null) ? nextItem.getSongId() : null;
            playState.setCurrentSongId(nextSongId);
            playState.setCurrentProgress(0);
            playStateMapper.updateById(playState);
        }
        queueCustomMapper.decrementSongCount(itemQueueId);
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCurrentQueueState(Long userId, UpdateCurrentQueueStateDTO dto) {

        Queues queue = queuesMapper.selectOne(new LambdaQueryWrapper<Queues>()
                .eq(Queues::getQueueId, dto.getCurrentQueueId())
                .eq(Queues::getCreatorId, userId));
        if (queue == null) {
            throw new BusinessException(404, "队列不存在或无权操作");
        }

        Integer finalPosition = 0;
        if (dto.getCurrentSongId() != null) {
            QueueItems item = queueItemsMapper.selectOne(new LambdaQueryWrapper<QueueItems>()
                    .eq(QueueItems::getQueueId, dto.getCurrentQueueId())
                    .eq(QueueItems::getSongId, dto.getCurrentSongId())
                    .last("LIMIT 1"));
            if (item == null) {
                throw new BusinessException(400, "当前歌曲不在该队列中");
            }
            finalPosition = item.getQueueItemPosition();
        }

        PlayState playState = playStateMapper.selectOne(new LambdaQueryWrapper<PlayState>()
                .eq(PlayState::getUserId, userId));

        if (playState == null) {
            playState = new PlayState();
            playState.setUserId(userId);
            mapDtoToEntity(playState, dto, finalPosition);
            playStateMapper.insert(playState);
        } else {
            mapDtoToEntity(playState, dto, finalPosition);
            playStateMapper.updateById(playState);
        }

    }

    private void mapDtoToEntity(PlayState ps, UpdateCurrentQueueStateDTO dto, Integer pos) {
        ps.setCurrentQueueId(dto.getCurrentQueueId());
        ps.setCurrentSongId(dto.getCurrentSongId());
        ps.setCurrentPosition(pos);

        ps.setCurrentProgress(dto.getCurrentProgress() != null ? dto.getCurrentProgress() : 0);

        ps.setUpdatedDate(LocalDateTime.now());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AlterQueueVO alterQueueToCurrent(Long userId, Long queueId) {
        Queues queue = queuesMapper.selectOne(new LambdaQueryWrapper<Queues>()
                .eq(Queues::getQueueId, queueId)
                .eq(Queues::getCreatorId, userId));

        if (queue == null) {
            throw new BusinessException(403, "队列不存在或无权访问");
        }

        QueueItems firstItem = queueItemsMapper.selectOne(new LambdaQueryWrapper<QueueItems>()
                .eq(QueueItems::getQueueId, queueId)
                .eq(QueueItems::getQueueItemPosition, 1)
                .last("LIMIT 1"));

        Long firstSongId = (firstItem != null) ? firstItem.getSongId() : null;

        PlayState playState = playStateMapper.selectOne(new LambdaQueryWrapper<PlayState>()
                .eq(PlayState::getUserId, userId));

        if (playState == null) {
            playState = new PlayState();
            playState.setUserId(userId);
            fillPlayState(playState, queueId, firstSongId);
            playStateMapper.insert(playState);
        } else {
            fillPlayState(playState, queueId, firstSongId);
            playStateMapper.updateById(playState);
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
        ps.setUpdatedDate(LocalDateTime.now());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setPlayMode(Long userId, Long queueId, String playMode) {
        Queues queue = queuesMapper.selectOne(new LambdaQueryWrapper<Queues>()
                .eq(Queues::getQueueId, queueId)
                .eq(Queues::getCreatorId, userId));

        if (queue == null) {
            throw new BusinessException(403, "无权操作此队列");
        }

        PlayState playState = playStateMapper.selectOne(new LambdaQueryWrapper<PlayState>()
                .eq(PlayState::getUserId, userId));

        if (playState == null) {
            playState = new PlayState();
            playState.setUserId(userId);
            playState.setCurrentQueueId(queueId);
            playState.setPlaymode(playMode);
            playState.setUpdatedDate(LocalDateTime.now());
            playStateMapper.insert(playState);
        } else {
            playState.setCurrentQueueId(queueId);
            playState.setPlaymode(playMode);
            playState.setUpdatedDate(LocalDateTime.now());
            playStateMapper.updateById(playState);
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reorderQueue(Long userId, Long queueId, List<Long> songIds) {
        Queues queue = queuesMapper.selectOne(new LambdaQueryWrapper<Queues>()
                .eq(Queues::getQueueId, queueId)
                .eq(Queues::getCreatorId, userId));

        if (queue == null) {
            throw new BusinessException(403, "无权操作此队列");
        }

        List<QueueItems> queueItems = queueItemsMapper.selectList(new LambdaQueryWrapper<QueueItems>()
                .eq(QueueItems::getQueueId, queueId));
        Set<Long> requestedSongIds = new HashSet<>(songIds);
        Set<Long> queueSongIds = new HashSet<>();
        for (QueueItems item : queueItems) {
            queueSongIds.add(item.getSongId());
        }
        if (requestedSongIds.size() != songIds.size() || !requestedSongIds.equals(queueSongIds)) {
            throw new BusinessException(400, "重排歌曲列表必须与队列内容一致");
        }

        queueCustomMapper.moveAllItemPositionsToTemporary(queueId);
        queueCustomMapper.batchUpdatePositions(queueId, songIds);

        queueCustomMapper.syncPlayStatePosition(userId, queueId);
    }
}
