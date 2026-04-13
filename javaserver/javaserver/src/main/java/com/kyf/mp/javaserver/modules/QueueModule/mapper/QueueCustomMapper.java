package com.kyf.mp.javaserver.modules.QueueModule.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.kyf.mp.javaserver.modules.QueueModule.VO.CurrentQueue;
import com.kyf.mp.javaserver.modules.QueueModule.VO.ReturnQueue;

import io.lettuce.core.dynamic.annotation.Param;

@Mapper
public interface QueueCustomMapper {
    /**
     * 根据用户ID查询当前播放状态、队列信息及歌曲列表
     */
    List<CurrentQueue> selectCurrentQueueDetail(@Param("userId") Integer userId);

    List<ReturnQueue> selectMyQueues(@Param("userId") Integer userId);

    ReturnQueue selectQueueById(@Param("queueId") Integer queueId);

    int copySongsFromPlaylistToQueue(@Param("queueId") Integer queueId, @Param("playlistId") Integer playlistId);

    void shiftItemPositions(@Param("queueId") Integer queueId, @Param("pos") int pos);

    void incrementSongCount(@Param("queueId") Integer queueId);

    void decrementSongCount(@Param("queueId") Integer queueId);

    void shiftPositionsDown(@Param("queueId") Integer queueId, @Param("pos") Integer pos);

    Map<String, Object> selectItemDetailForDelete(@Param("itemId") Integer itemId, @Param("userId") Integer userId);

    void batchUpdatePositions(@Param("queueId") Integer queueId, @Param("songIds") List<Integer> songIds);

    void syncPlayStatePosition(@Param("userId") Integer userId, @Param("queueId") Integer queueId);
}