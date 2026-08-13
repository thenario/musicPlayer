package com.kyf.mp.server.modules.playlist.business.imp;

import org.springframework.stereotype.Service;

import com.kyf.mp.server.common.business.BaseBusinessImpl;
import com.kyf.mp.server.modules.playlist.business.UsersLikeplaylistsRelationBusiness;
import com.kyf.mp.server.modules.playlist.entity.UsersLikeplaylistsRelation;
import com.kyf.mp.server.modules.playlist.mapper.UsersLikeplaylistsRelationMapper;

/**
 * 用户-歌单点赞关联数据访问实现。
 */
@Service
public class UsersLikeplaylistsRelationBusinessImpl
        extends BaseBusinessImpl<UsersLikeplaylistsRelationMapper, UsersLikeplaylistsRelation>
        implements UsersLikeplaylistsRelationBusiness {
}
