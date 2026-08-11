package com.kyf.mp.javaserver.modules.queuemodule.businessImp;

import org.springframework.stereotype.Service;

import com.kyf.mp.javaserver.common.business.BaseBusinessImpl;
import com.kyf.mp.javaserver.modules.queuemodule.business.IPlayStateBusiness;
import com.kyf.mp.javaserver.modules.queuemodule.entity.PlayState;
import com.kyf.mp.javaserver.modules.queuemodule.mapper.PlayStateMapper;

/**
 * 播放状态数据访问实现。
 */
@Service
public class PlayStateBusinessImpl extends BaseBusinessImpl<PlayStateMapper, PlayState> implements IPlayStateBusiness {
}
