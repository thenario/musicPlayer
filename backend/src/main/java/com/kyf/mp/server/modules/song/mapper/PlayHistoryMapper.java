package com.kyf.mp.server.modules.song.mapper;

import java.util.List;

import com.kyf.mp.server.modules.queue.entity.PlayHistory;
import com.kyf.mp.server.modules.song.vo.PlayHistoryVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;


public interface PlayHistoryMapper extends BaseMapper<PlayHistory> {
    List<PlayHistoryVO> selectHistoryWithSong(@Param("userId") Long userId);
}
