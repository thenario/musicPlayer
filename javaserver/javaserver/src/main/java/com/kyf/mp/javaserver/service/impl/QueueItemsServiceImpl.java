package com.kyf.mp.javaserver.service.impl;

import com.kyf.mp.javaserver.entity.QueueItems;
import com.kyf.mp.javaserver.mapper.QueueItemsMapper;
import com.kyf.mp.javaserver.service.IQueueItemsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author kyf
 * @since 2026-04-05
 */
@Service
public class QueueItemsServiceImpl extends ServiceImpl<QueueItemsMapper, QueueItems> implements IQueueItemsService {

}
