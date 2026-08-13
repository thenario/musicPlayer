package com.kyf.mp.server.modules.song.business.imp;

import org.springframework.stereotype.Service;

import com.kyf.mp.server.common.business.BaseBusinessImpl;
import com.kyf.mp.server.modules.song.business.SongsTagsRelationBusiness;
import com.kyf.mp.server.modules.song.entity.SongsTagsRelation;
import com.kyf.mp.server.modules.song.mapper.SongsTagsRelationMapper;

/**
 * 歌曲-标签关联数据访问实现。
 */
@Service
public class SongsTagsRelationBusinessImpl extends BaseBusinessImpl<SongsTagsRelationMapper, SongsTagsRelation>
        implements SongsTagsRelationBusiness {
}
