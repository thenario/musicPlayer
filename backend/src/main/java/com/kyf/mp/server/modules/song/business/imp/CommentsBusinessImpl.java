package com.kyf.mp.server.modules.song.business.imp;

import org.springframework.stereotype.Service;

import com.kyf.mp.server.common.business.BaseBusinessImpl;
import com.kyf.mp.server.modules.song.business.CommentsBusiness;
import com.kyf.mp.server.modules.song.entity.Comments;
import com.kyf.mp.server.modules.song.mapper.CommentsMapper;

/**
 * 评论数据访问实现。
 */
@Service
public class CommentsBusinessImpl extends BaseBusinessImpl<CommentsMapper, Comments> implements CommentsBusiness {
}
