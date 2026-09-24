package com.kyf.mp.server.modules.song.repository.impl;

import org.springframework.stereotype.Repository;

import com.kyf.mp.server.common.repository.BaseRepositoryImpl;
import com.kyf.mp.server.modules.song.entity.Comments;
import com.kyf.mp.server.modules.song.mapper.CommentsMapper;
import com.kyf.mp.server.modules.song.repository.CommentsRepository;

/** Comments 数据访问实现。 */
@Repository
public class CommentsRepositoryImpl extends BaseRepositoryImpl<CommentsMapper, Comments> implements CommentsRepository {

    public CommentsRepositoryImpl(CommentsMapper mapper) {
        super(mapper);
    }
}
