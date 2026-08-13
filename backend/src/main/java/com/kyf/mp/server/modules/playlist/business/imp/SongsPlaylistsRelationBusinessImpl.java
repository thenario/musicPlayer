package com.kyf.mp.server.modules.playlist.business.imp;

import org.springframework.stereotype.Service;

import com.kyf.mp.server.common.business.BaseBusinessImpl;
import com.kyf.mp.server.modules.playlist.business.SongsPlaylistsRelationBusiness;
import com.kyf.mp.server.modules.playlist.entity.SongsPlaylistsRelation;
import com.kyf.mp.server.modules.playlist.mapper.SongsPlaylistsRelationMapper;

/**
 * 歌单-歌曲关联数据访问实现。
 */
@Service
public class SongsPlaylistsRelationBusinessImpl
        extends BaseBusinessImpl<SongsPlaylistsRelationMapper, SongsPlaylistsRelation>
        implements SongsPlaylistsRelationBusiness {
}
