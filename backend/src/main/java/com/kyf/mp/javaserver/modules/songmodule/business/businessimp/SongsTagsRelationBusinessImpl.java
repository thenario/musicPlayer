package com.kyf.mp.javaserver.modules.songmodule.business.businessimp;

import org.springframework.stereotype.Service;

import com.kyf.mp.javaserver.common.business.BaseBusinessImpl;
import com.kyf.mp.javaserver.modules.songmodule.business.ISongsTagsRelationBusiness;
import com.kyf.mp.javaserver.modules.songmodule.entity.SongsTagsRelation;
import com.kyf.mp.javaserver.modules.songmodule.mapper.SongsTagsRelationMapper;

/**
 * 歌曲-标签关联数据访问实现。
 */
@Service
public class SongsTagsRelationBusinessImpl extends BaseBusinessImpl<SongsTagsRelationMapper, SongsTagsRelation>
        implements ISongsTagsRelationBusiness {
}
