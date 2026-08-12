package com.kyf.mp.javaserver.modules.playlistmodule.business.businessimp;

import org.springframework.stereotype.Service;

import com.kyf.mp.javaserver.common.business.BaseBusinessImpl;
import com.kyf.mp.javaserver.modules.playlistmodule.business.IUsersLikeplaylistsRelationBusiness;
import com.kyf.mp.javaserver.modules.playlistmodule.entity.UsersLikeplaylistsRelation;
import com.kyf.mp.javaserver.modules.playlistmodule.mapper.UsersLikeplaylistsRelationMapper;

/**
 * 用户-歌单点赞关联数据访问实现。
 */
@Service
public class UsersLikeplaylistsRelationBusinessImpl
        extends BaseBusinessImpl<UsersLikeplaylistsRelationMapper, UsersLikeplaylistsRelation>
        implements IUsersLikeplaylistsRelationBusiness {
}
