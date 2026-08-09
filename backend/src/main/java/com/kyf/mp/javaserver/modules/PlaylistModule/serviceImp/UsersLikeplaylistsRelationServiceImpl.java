package com.kyf.mp.javaserver.modules.PlaylistModule.serviceImp;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kyf.mp.javaserver.modules.PlaylistModule.entity.UsersLikeplaylistsRelation;
import com.kyf.mp.javaserver.modules.PlaylistModule.mapper.UsersLikeplaylistsRelationMapper;
import com.kyf.mp.javaserver.modules.PlaylistModule.service.IUsersLikeplaylistsRelationService;

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
