package com.kyf.mp.javaserver.modules.PlaylistModule.serviceImp;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kyf.mp.javaserver.modules.PlaylistModule.entity.UsersPlaylistsRelation;
import com.kyf.mp.javaserver.modules.PlaylistModule.mapper.UsersPlaylistsRelationMapper;
import com.kyf.mp.javaserver.modules.PlaylistModule.service.IUsersPlaylistsRelationService;

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
public class UsersPlaylistsRelationServiceImpl extends ServiceImpl<UsersPlaylistsRelationMapper, UsersPlaylistsRelation>
        implements IUsersPlaylistsRelationService {

}
