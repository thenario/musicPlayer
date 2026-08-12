package com.kyf.mp.javaserver.modules.playlistmodule.business.businessimp;

import org.springframework.stereotype.Service;

import com.kyf.mp.javaserver.common.business.BaseBusinessImpl;
import com.kyf.mp.javaserver.modules.playlistmodule.business.ISongsPlaylistsRelationBusiness;
import com.kyf.mp.javaserver.modules.playlistmodule.entity.SongsPlaylistsRelation;
import com.kyf.mp.javaserver.modules.playlistmodule.mapper.SongsPlaylistsRelationMapper;

/**
 * 歌单-歌曲关联数据访问实现。
 */
@Service
public class SongsPlaylistsRelationBusinessImpl
        extends BaseBusinessImpl<SongsPlaylistsRelationMapper, SongsPlaylistsRelation>
        implements ISongsPlaylistsRelationBusiness {
}
