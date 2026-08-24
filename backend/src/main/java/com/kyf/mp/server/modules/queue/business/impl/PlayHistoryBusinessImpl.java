package com.kyf.mp.server.modules.queue.business.impl;

import org.springframework.stereotype.Service;

import com.kyf.mp.server.common.business.BaseBusinessImpl;
import com.kyf.mp.server.modules.queue.business.PlayHistoryBusiness;
import com.kyf.mp.server.modules.queue.entity.PlayHistory;
import com.kyf.mp.server.modules.queue.mapper.PlayHistoryMapper;

/**
 * 播放历史数据访问实现。
 */
@Service
public class PlayHistoryBusinessImpl extends BaseBusinessImpl<PlayHistoryMapper, PlayHistory>
                implements PlayHistoryBusiness {
}
