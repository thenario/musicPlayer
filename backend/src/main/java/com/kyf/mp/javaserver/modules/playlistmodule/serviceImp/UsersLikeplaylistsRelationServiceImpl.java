package com.kyf.mp.javaserver.modules.playlistmodule.serviceImp;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kyf.mp.javaserver.modules.playlistmodule.entity.UsersLikeplaylistsRelation;
import com.kyf.mp.javaserver.modules.playlistmodule.mapper.UsersLikeplaylistsRelationMapper;
import com.kyf.mp.javaserver.modules.playlistmodule.service.IUsersLikeplaylistsRelationService;

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
public class UsersLikeplaylistsRelationServiceImpl
        extends ServiceImpl<UsersLikeplaylistsRelationMapper, UsersLikeplaylistsRelation>
        implements IUsersLikeplaylistsRelationService {

}
