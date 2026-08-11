package com.kyf.mp.javaserver.modules.queuemodule.businessImp;

import org.springframework.stereotype.Service;

import com.kyf.mp.javaserver.common.business.BaseBusinessImpl;
import com.kyf.mp.javaserver.modules.queuemodule.business.IQueueItemsBusiness;
import com.kyf.mp.javaserver.modules.queuemodule.entity.QueueItems;
import com.kyf.mp.javaserver.modules.queuemodule.mapper.QueueItemsMapper;

/**
 * 队列条目数据访问实现。
 */
@Service
public class QueueItemsBusinessImpl extends BaseBusinessImpl<QueueItemsMapper, QueueItems>
        implements IQueueItemsBusiness {
}
