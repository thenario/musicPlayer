package com.kyf.mp.javaserver.modules.songmodule.business.businessimp;

import org.springframework.stereotype.Service;

import com.kyf.mp.javaserver.common.business.BaseBusinessImpl;
import com.kyf.mp.javaserver.modules.songmodule.business.ICommentsBusiness;
import com.kyf.mp.javaserver.modules.songmodule.entity.Comments;
import com.kyf.mp.javaserver.modules.songmodule.mapper.CommentsMapper;

/**
 * 评论数据访问实现。
 */
@Service
public class CommentsBusinessImpl extends BaseBusinessImpl<CommentsMapper, Comments> implements ICommentsBusiness {
}
