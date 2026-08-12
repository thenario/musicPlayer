package com.kyf.mp.javaserver.modules.songmodule.business.businessimp;

import org.springframework.stereotype.Service;

import com.kyf.mp.javaserver.common.business.BaseBusinessImpl;
import com.kyf.mp.javaserver.modules.songmodule.business.ITagsBusiness;
import com.kyf.mp.javaserver.modules.songmodule.entity.Tags;
import com.kyf.mp.javaserver.modules.songmodule.mapper.TagsMapper;

/**
 * 标签数据访问实现。
 */
@Service
public class TagsBusinessImpl extends BaseBusinessImpl<TagsMapper, Tags> implements ITagsBusiness {
}
