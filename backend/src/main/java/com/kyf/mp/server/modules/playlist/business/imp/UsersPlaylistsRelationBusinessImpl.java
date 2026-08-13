package com.kyf.mp.server.modules.playlist.business.imp;

import org.springframework.stereotype.Service;

import com.kyf.mp.server.common.business.BaseBusinessImpl;
import com.kyf.mp.server.modules.playlist.business.UsersPlaylistsRelationBusiness;
import com.kyf.mp.server.modules.playlist.entity.UsersPlaylistsRelation;
import com.kyf.mp.server.modules.playlist.mapper.UsersPlaylistsRelationMapper;

/**
 * 用户-歌单关联数据访问实现。
 */
@Service
public class UsersPlaylistsRelationBusinessImpl
        extends BaseBusinessImpl<UsersPlaylistsRelationMapper, UsersPlaylistsRelation>
        implements UsersPlaylistsRelationBusiness {
}
