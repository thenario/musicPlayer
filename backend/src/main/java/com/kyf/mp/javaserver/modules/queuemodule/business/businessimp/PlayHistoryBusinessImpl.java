package com.kyf.mp.javaserver.modules.queuemodule.business.businessimp;

import org.springframework.stereotype.Service;

import com.kyf.mp.javaserver.common.business.BaseBusinessImpl;
import com.kyf.mp.javaserver.modules.queuemodule.business.PlayHistoryBusiness;
import com.kyf.mp.javaserver.modules.queuemodule.entity.PlayHistory;
import com.kyf.mp.javaserver.modules.queuemodule.mapper.PlayHistoryMapper;

/**
 * 播放历史数据访问实现。
 */
@Service
public class PlayHistoryBusinessImpl extends BaseBusinessImpl<PlayHistoryMapper, PlayHistory>
        implements PlayHistoryBusiness {
}
