package com.kyf.mp.javaserver.modules.playlistmodule.businessImp;

import org.springframework.stereotype.Service;

import com.kyf.mp.javaserver.common.business.BaseBusinessImpl;
import com.kyf.mp.javaserver.modules.playlistmodule.business.IUsersPlaylistsRelationBusiness;
import com.kyf.mp.javaserver.modules.playlistmodule.entity.UsersPlaylistsRelation;
import com.kyf.mp.javaserver.modules.playlistmodule.mapper.UsersPlaylistsRelationMapper;

/**
 * 用户-歌单关联数据访问实现。
 */
@Service
public class UsersPlaylistsRelationBusinessImpl
        extends BaseBusinessImpl<UsersPlaylistsRelationMapper, UsersPlaylistsRelation>
        implements IUsersPlaylistsRelationBusiness {
}
