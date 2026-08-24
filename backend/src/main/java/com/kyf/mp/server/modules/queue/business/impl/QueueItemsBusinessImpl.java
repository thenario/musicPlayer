package com.kyf.mp.server.modules.queue.business.impl;

import org.springframework.stereotype.Service;

import com.kyf.mp.server.common.business.BaseBusinessImpl;
import com.kyf.mp.server.modules.queue.business.QueueItemsBusiness;
import com.kyf.mp.server.modules.queue.entity.QueueItems;
import com.kyf.mp.server.modules.queue.mapper.QueueItemsMapper;

/**
 * 队列条目数据访问实现。
 */
@Service
public class QueueItemsBusinessImpl extends BaseBusinessImpl<QueueItemsMapper, QueueItems>
                implements QueueItemsBusiness {
}
