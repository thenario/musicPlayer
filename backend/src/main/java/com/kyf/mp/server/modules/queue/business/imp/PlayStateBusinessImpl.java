package com.kyf.mp.server.modules.queue.business.imp;

import org.springframework.stereotype.Service;

import com.kyf.mp.server.common.business.BaseBusinessImpl;
import com.kyf.mp.server.modules.queue.business.PlayStateBusiness;
import com.kyf.mp.server.modules.queue.entity.PlayState;
import com.kyf.mp.server.modules.queue.mapper.PlayStateMapper;

/**
 * 播放状态数据访问实现。
 */
@Service
public class PlayStateBusinessImpl extends BaseBusinessImpl<PlayStateMapper, PlayState> implements PlayStateBusiness {
}
