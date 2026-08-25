package com.kyf.mp.server.modules.queue.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.kyf.mp.server.modules.queue.vo.CurrentQueueVO;
import com.kyf.mp.server.modules.queue.vo.ReturnQueueVO;

@Mapper
public interface QueueCustomMapper {
    /**
     * 根据用户ID查询当前播放状态、队列信息及歌曲列表
     */
    List<CurrentQueueVO> selectCurrentQueueDetail(@Param("userId") Long userId);

    List<ReturnQueueVO> selectMyQueues(@Param("userId") Long userId);

    ReturnQueueVO selectQueueById(@Param("queueId") Long queueId, @Param("userId") Long userId);

    void moveItemPositionsToTemporary(@Param("queueId") Long queueId, @Param("pos") int pos);

    void restoreShiftedItemPositions(@Param("queueId") Long queueId, @Param("pos") int pos);

    void incrementSongCount(@Param("queueId") Long queueId);

    void decrementSongCount(@Param("queueId") Long queueId);

    void shiftPositionsDown(@Param("queueId") Long queueId, @Param("pos") Integer pos);

    Map<String, Object> selectItemDetailForDelete(@Param("itemId") Long itemId, @Param("userId") Long userId);

    void batchUpdatePositions(@Param("queueId") Long queueId, @Param("songIds") List<Long> songIds);

    void moveAllItemPositionsToTemporary(@Param("queueId") Long queueId);

    void syncPlayStatePosition(@Param("userId") Long userId, @Param("queueId") Long queueId);
}
