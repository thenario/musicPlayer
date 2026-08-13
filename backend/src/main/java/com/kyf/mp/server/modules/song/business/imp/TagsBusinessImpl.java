package com.kyf.mp.server.modules.song.business.imp;

import org.springframework.stereotype.Service;

import com.kyf.mp.server.common.business.BaseBusinessImpl;
import com.kyf.mp.server.modules.song.business.TagsBusiness;
import com.kyf.mp.server.modules.song.entity.Tags;
import com.kyf.mp.server.modules.song.mapper.TagsMapper;

/**
 * 标签数据访问实现。
 */
@Service
public class TagsBusinessImpl extends BaseBusinessImpl<TagsMapper, Tags> implements TagsBusiness {
}
