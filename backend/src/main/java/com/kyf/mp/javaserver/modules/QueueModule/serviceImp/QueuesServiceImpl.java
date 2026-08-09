package com.kyf.mp.javaserver.modules.QueueModule.serviceImp;

import com.kyf.mp.javaserver.common.BusinessException;
import com.kyf.mp.javaserver.common.ResultModel;
import com.kyf.mp.javaserver.modules.PlaylistModule.entity.Playlists;
import com.kyf.mp.javaserver.modules.PlaylistModule.mapper.PlaylistsMapper;
import com.kyf.mp.javaserver.modules.QueueModule.DTO.AddSongToQueue;
import com.kyf.mp.javaserver.modules.QueueModule.DTO.UpdateCurrentQueueStateDTO;
import com.kyf.mp.javaserver.modules.QueueModule.VO.AddSongToQueueVO;
import com.kyf.mp.javaserver.modules.QueueModule.VO.AlterQueueVO;
import com.kyf.mp.javaserver.modules.QueueModule.VO.CreateQueueFromPlaylist;
import com.kyf.mp.javaserver.modules.QueueModule.VO.CurrentQueue;
import com.kyf.mp.javaserver.modules.QueueModule.VO.DeleteQueueVO;
import com.kyf.mp.javaserver.modules.QueueModule.VO.MyQueues;
import com.kyf.mp.javaserver.modules.QueueModule.VO.ReturnQueue;
import com.kyf.mp.javaserver.modules.QueueModule.VO.SingleQueue;
import com.kyf.mp.javaserver.modules.QueueModule.entity.PlayState;
import com.kyf.mp.javaserver.modules.QueueModule.entity.QueueItems;
import com.kyf.mp.javaserver.modules.QueueModule.entity.Queues;
import com.kyf.mp.javaserver.modules.QueueModule.mapper.PlayStateMapper;
import com.kyf.mp.javaserver.modules.QueueModule.mapper.QueueCustomMapper;
import com.kyf.mp.javaserver.modules.QueueModule.mapper.QueueItemsMapper;
import com.kyf.mp.javaserver.modules.QueueModule.mapper.QueuesMapper;
import com.kyf.mp.javaserver.modules.QueueModule.service.IQueuesService;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
@RequiredArgsConstructor
public class QueuesServiceImpl extends ServiceImpl<QueuesMapper, Queues> implements IQueuesService {
    private final QueueCustomMapper queueCustomMapper;
    private final PlayStateMapper playStateMapper;
    private final QueueItemsMapper queueItemsMapper;
    private final QueuesMapper queuesMapper;
    private final PlaylistsMapper playlistsMapper;

    @Override
    public ResultModel<CurrentQueue> getCurrentQueue(Integer userId) {
        if (userId == null) {
            throw new BusinessException(401, "用户未登录");
        }

        try {
            List<CurrentQueue> list = queueCustomMapper.selectCurrentQueueDetail(userId);

            if (list == null || list.isEmpty()) {
                return ResultModel.success(null);
            }

            CurrentQueue result = list.get(0);

            if (result == null || result.getQueueState() == null) {
                return ResultModel.success(null);
            }

            return ResultModel.success(result);

        } catch (Exception e) {
            log.error("获取当前队列 SQL 异常: ", e);
            throw new BusinessException(500, "获取失败");
        }
    }

    @Override
    public ResultModel<MyQueues> getMyQueues(Integer userId) {
        if (userId == null)
            throw new BusinessException(401, "用户未登录");

        List<ReturnQueue> list = queueCustomMapper.selectMyQueues(userId);

        MyQueues result = new MyQueues();
        result.setQueues(list != null ? list : new ArrayList<>());

        return ResultModel.success(result);
    }

    @Override
    public ResultModel<SingleQueue> getQueueById(Integer queueId) {
        ReturnQueue queueDetail = queueCustomMapper.selectQueueById(queueId);

        if (queueDetail == null) {
            throw new BusinessException(404, "队列不存在");
        }

        SingleQueue vo = new SingleQueue();
        vo.setQueue(queueDetail);

        return ResultModel.success(vo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultModel<DeleteQueueVO> deleteQueue(Integer userId, Integer queueId) {
        if (queueId == null || userId == null) {
            throw new BusinessException(400, "参数错误");
        }

        try {
            PlayState playState = playStateMapper.selectOne(
                    new LambdaQueryWrapper<PlayState>().eq(PlayState::getUserId, userId));

            boolean isActive = playState != null && queueId.equals(playState.getCurrentQueueId());

            queueItemsMapper.delete(
                    new LambdaQueryWrapper<QueueItems>().eq(QueueItems::getQueueId, queueId));

            int affectedRows = queuesMapper.delete(
                    new LambdaQueryWrapper<Queues>()
                            .eq(Queues::getQueueId, queueId)
                            .eq(Queues::getCreatorId, userId));

            if (affectedRows == 0) {
                throw new BusinessException(500, "删除失败，队列不存在或无权限");
            }

            Integer newQueueId = null;

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

            return ResultModel.success(vo);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除队列异常: ", e);
            throw new BusinessException(500, "服务器错误: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultModel<Void> clearQueue(Integer userId, Integer queueId) {
        // 1. 健壮性检查：防御其他 Service 的错误调用
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

        return ResultModel.success(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultModel<CreateQueueFromPlaylist> createQueueFromPlaylist(Integer userId, Integer playlistId) {
        // 1. 参数校验
        if (userId == null || playlistId == null || playlistId <= 0) {
            throw new BusinessException(400, "参数错误，歌单ID不能为空");
        }

        try {
            // 2. 检查队列上限（限制为5个，对应 Node 的限制逻辑）
            List<Queues> existingQueues = queuesMapper.selectList(new LambdaQueryWrapper<Queues>()
                    .eq(Queues::getCreatorId, userId)
                    .orderByAsc(Queues::getCreatedDate));

            if (existingQueues != null && existingQueues.size() >= 5) {
                // 删除最早创建的那个队列
                Integer oldestId = existingQueues.get(0).getQueueId();
                // 注意：如果有外键级联删除，直接删 queues；否则先删 queue_items
                queueItemsMapper.delete(new LambdaQueryWrapper<QueueItems>().eq(QueueItems::getQueueId, oldestId));
                queuesMapper.deleteById(oldestId);
            }

            // 3. 获取歌单名称作为新队列名称
            Playlists playlist = playlistsMapper.selectById(playlistId);
            String queueName = (playlist != null) ? playlist.getPlaylistName() : "新播放队列";

            // 4. 创建新队列主体
            Queues newQueue = new Queues();
            newQueue.setQueueName(queueName);
            newQueue.setCreatorId(userId);
            newQueue.setIsCurrent(true); // 设为当前队列
            newQueue.setSongCount(0);
            queuesMapper.insert(newQueue);
            Integer newQueueId = newQueue.getQueueId();

            // 5. 将歌单中的歌曲导入 queue_items (调用 XML 中的自定义插入)
            // 对应 Node: INSERT INTO queue_items ... SELECT ... FROM songs_playlists_relation
            int insertedSongs = queueCustomMapper.copySongsFromPlaylistToQueue(newQueueId, playlistId);

            // 6. 更新新队列的歌曲总数
            newQueue.setSongCount(insertedSongs);
            queuesMapper.updateById(newQueue);

            // 7. 更新播放状态 (PlayState)
            PlayState playState = playStateMapper.selectOne(new LambdaQueryWrapper<PlayState>()
                    .eq(PlayState::getUserId, userId));

            if (playState != null) {
                playState.setCurrentQueueId(newQueueId);
                playState.setCurrentSongId(null);
                playState.setCurrentProgress(0);
                playStateMapper.updateById(playState);
            }

            // 8. 组装返回 VO
            CreateQueueFromPlaylist vo = new CreateQueueFromPlaylist();
            vo.setQueueId(newQueueId);
            vo.setSongCount(insertedSongs);

            return ResultModel.success(vo);

        } catch (Exception e) {
            log.error("从歌单创建队列失败: ", e);
            throw new BusinessException(500, "创建队列失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultModel<AddSongToQueueVO> addSongToQueue(Integer userId, Integer paramQueueId, AddSongToQueue dto) {
        Integer songId = dto.getSongId();
        boolean mode = dto.getMode() != null && dto.getMode();

        QueueContext context = ensureQueueId(userId, paramQueueId);
        Integer finalQueueId = context.getFinalQueueId();

        PlayState playState = playStateMapper.selectOne(new LambdaQueryWrapper<PlayState>()
                .eq(PlayState::getUserId, userId));

        int currentPos = (playState != null) ? playState.getCurrentPosition() : 0;
        boolean hasState = (playState != null);

        int insertPos = mode ? currentPos + 1 : currentPos;

        int deleted = queueItemsMapper.delete(new LambdaQueryWrapper<QueueItems>()
                .eq(QueueItems::getQueueId, finalQueueId)
                .eq(QueueItems::getSongId, songId));
        boolean wasExisted = deleted > 0;

        queueCustomMapper.shiftItemPositions(finalQueueId, insertPos);

        QueueItems newItem = new QueueItems();
        newItem.setQueueId(finalQueueId);
        newItem.setSongId(songId);
        newItem.setQueueItemPosition(insertPos);
        newItem.setAddedDate(LocalDateTime.now());
        queueItemsMapper.insert(newItem);

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

        return ResultModel.success(vo);
    }

    // 内部辅助逻辑

    private QueueContext ensureQueueId(Integer userId, Integer qId) {
        if (qId != null && qId > 0) {
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
        private Integer finalQueueId;
        private boolean isNewQueue;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultModel<Void> removeSongFromQueue(Integer userId, Integer queueItemId) {
        Map<String, Object> itemInfo = queueCustomMapper.selectItemDetailForDelete(queueItemId, userId);

        if (itemInfo == null || itemInfo.isEmpty()) {
            throw new BusinessException(404, "未找到该歌曲或无权操作");
        }

        Integer queueId = (Integer) itemInfo.get("queue_id");
        Integer removedPos = (Integer) itemInfo.get("queue_item_position");
        Integer removedSongId = (Integer) itemInfo.get("song_id");

        queueItemsMapper.deleteById(queueItemId);
        queueCustomMapper.shiftPositionsDown(queueId, removedPos);

        PlayState playState = playStateMapper.selectOne(new LambdaQueryWrapper<PlayState>()
                .eq(PlayState::getUserId, userId)
                .eq(PlayState::getCurrentQueueId, queueId));

        if (playState != null && removedSongId.equals(playState.getCurrentSongId())) {
            QueueItems nextItem = queueItemsMapper.selectOne(new LambdaQueryWrapper<QueueItems>()
                    .eq(QueueItems::getQueueId, queueId)
                    .eq(QueueItems::getQueueItemPosition, removedPos)
                    .last("LIMIT 1"));

            Integer nextSongId = (nextItem != null) ? nextItem.getSongId() : null;

            playState.setCurrentSongId(nextSongId);
            playState.setCurrentProgress(0);
            playStateMapper.updateById(playState);
        }
        queueCustomMapper.decrementSongCount(queueId);

        return ResultModel.success(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultModel<Void> updateCurrentQueueState(Integer userId, UpdateCurrentQueueStateDTO dto) {

        Integer finalPosition = dto.getCurrentPosition();

        if (finalPosition == null && dto.getCurrentSongId() != null) {
            QueueItems item = queueItemsMapper.selectOne(new LambdaQueryWrapper<QueueItems>()
                    .eq(QueueItems::getQueueId, dto.getCurrentQueueId())
                    .eq(QueueItems::getSongId, dto.getCurrentSongId())
                    .last("LIMIT 1"));

            if (item != null) {
                finalPosition = item.getQueueItemPosition();
            }
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

        return ResultModel.success(null);
    }

    /**
     * 辅助方法：将 DTO 数据映射到数据库实体类
     */
    private void mapDtoToEntity(PlayState ps, UpdateCurrentQueueStateDTO dto, Integer pos) {
        ps.setCurrentQueueId(dto.getCurrentQueueId());
        ps.setCurrentSongId(dto.getCurrentSongId());
        ps.setCurrentPosition(pos);

        ps.setCurrentProgress(dto.getCurrentProgress() != null ? dto.getCurrentProgress() : 0);

        ps.setUpdatedDate(LocalDateTime.now());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultModel<AlterQueueVO> alterQueueToCurrent(Integer userId, Integer queueId) {
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

        Integer firstSongId = (firstItem != null) ? firstItem.getSongId() : null;

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

        return ResultModel.success(vo);
    }

    private void fillPlayState(PlayState ps, Integer queueId, Integer songId) {
        ps.setCurrentQueueId(queueId);
        ps.setCurrentSongId(songId);
        ps.setCurrentPosition(1);
        ps.setCurrentProgress(0);
        ps.setUpdatedDate(LocalDateTime.now());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultModel<Void> setPlayMode(Integer userId, Integer queueId, String playMode) {
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

        return ResultModel.success(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultModel<Void> reorderQueue(Integer userId, Integer queueId, List<Integer> songIds) {
        Queues queue = queuesMapper.selectOne(new LambdaQueryWrapper<Queues>()
                .eq(Queues::getQueueId, queueId)
                .eq(Queues::getCreatorId, userId));

        if (queue == null) {
            throw new BusinessException(403, "无权操作此队列");
        }

        queueCustomMapper.batchUpdatePositions(queueId, songIds);

        queueCustomMapper.syncPlayStatePosition(userId, queueId);

        return ResultModel.success(null);
    }
}
