package com.kyf.mp.server.modules.song.business.impl;

import org.springframework.stereotype.Service;

import com.kyf.mp.server.common.business.BaseBusinessImpl;
import com.kyf.mp.server.modules.song.business.AlbumsBusiness;
import com.kyf.mp.server.modules.song.entity.Albums;
import com.kyf.mp.server.modules.song.mapper.AlbumsMapper;

/**
 * 专辑数据访问实现。
 */
@Service
public class AlbumsBusinessImpl extends BaseBusinessImpl<AlbumsMapper, Albums> implements AlbumsBusiness {
}
