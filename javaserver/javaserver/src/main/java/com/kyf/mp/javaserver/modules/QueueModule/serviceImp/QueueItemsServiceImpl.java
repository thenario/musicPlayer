package com.kyf.mp.javaserver.modules.QueueModule.serviceImp;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kyf.mp.javaserver.modules.QueueModule.entity.QueueItems;
import com.kyf.mp.javaserver.modules.QueueModule.mapper.QueueItemsMapper;
import com.kyf.mp.javaserver.modules.QueueModule.service.IQueueItemsService;

import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author kyf
 * @since 2026-04-05
 */
@Service
public class QueueItemsServiceImpl extends ServiceImpl<QueueItemsMapper, QueueItems> implements IQueueItemsService {

}
