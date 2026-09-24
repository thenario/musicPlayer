package com.kyf.mp.server.modules.song.repository.impl;

import org.springframework.stereotype.Repository;

import com.kyf.mp.server.common.repository.BaseRepositoryImpl;
import com.kyf.mp.server.modules.song.entity.Tags;
import com.kyf.mp.server.modules.song.mapper.TagsMapper;
import com.kyf.mp.server.modules.song.repository.TagsRepository;

/** Tags 数据访问实现。 */
@Repository
public class TagsRepositoryImpl extends BaseRepositoryImpl<TagsMapper, Tags> implements TagsRepository {

    public TagsRepositoryImpl(TagsMapper mapper) {
        super(mapper);
    }
}
