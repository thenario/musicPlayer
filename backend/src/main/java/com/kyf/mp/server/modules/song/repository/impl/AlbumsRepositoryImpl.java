package com.kyf.mp.server.modules.song.repository.impl;

import org.springframework.stereotype.Repository;

import com.kyf.mp.server.common.repository.BaseRepositoryImpl;
import com.kyf.mp.server.modules.song.entity.Albums;
import com.kyf.mp.server.modules.song.mapper.AlbumsMapper;
import com.kyf.mp.server.modules.song.repository.AlbumsRepository;

/** Albums 数据访问实现。 */
@Repository
public class AlbumsRepositoryImpl extends BaseRepositoryImpl<AlbumsMapper, Albums> implements AlbumsRepository {

    public AlbumsRepositoryImpl(AlbumsMapper mapper) {
        super(mapper);
    }
}
