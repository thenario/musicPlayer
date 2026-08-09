package com.kyf.mp.javaserver.modules.queuemodule.serviceImp;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kyf.mp.javaserver.modules.queuemodule.entity.PlayState;
import com.kyf.mp.javaserver.modules.queuemodule.mapper.PlayStateMapper;
import com.kyf.mp.javaserver.modules.queuemodule.service.IPlayStateService;

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
public class PlayStateServiceImpl extends ServiceImpl<PlayStateMapper, PlayState> implements IPlayStateService {

}
