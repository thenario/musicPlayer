package com.kyf.mp.javaserver.modules.queuemodule.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.kyf.mp.javaserver.modules.queuemodule.vo.CurrentQueue;
import com.kyf.mp.javaserver.modules.queuemodule.vo.ReturnQueue;

import io.lettuce.core.dynamic.annotation.Param;

@Mapper
public interface QueueCustomMapper {
    /**
     * 根据用户ID查询当前播放状态、队列信息及歌曲列表
     */
    List<CurrentQueue> selectCurrentQueueDetail(@Param("userId") Long userId);

    List<ReturnQueue> selectMyQueues(@Param("userId") Long userId);

    ReturnQueue selectQueueById(@Param("queueId") Long queueId);

    int copySongsFromPlaylistToQueue(@Param("queueId") Long queueId, @Param("playlistId") Long playlistId);

    void shiftItemPositions(@Param("queueId") Long queueId, @Param("pos") int pos);

    void incrementSongCount(@Param("queueId") Long queueId);

    void decrementSongCount(@Param("queueId") Long queueId);

    void shiftPositionsDown(@Param("queueId") Long queueId, @Param("pos") Integer pos);

    Map<String, Object> selectItemDetailForDelete(@Param("itemId") Long itemId, @Param("userId") Long userId);

    void batchUpdatePositions(@Param("queueId") Long queueId, @Param("songIds") List<Long> songIds);

    void syncPlayStatePosition(@Param("userId") Long userId, @Param("queueId") Long queueId);
}
