package com.kyf.mp.javaserver.modules.songmodule.business.businessimp;

import org.springframework.stereotype.Service;

import com.kyf.mp.javaserver.common.business.BaseBusinessImpl;
import com.kyf.mp.javaserver.modules.songmodule.business.IAlbumsBusiness;
import com.kyf.mp.javaserver.modules.songmodule.entity.Albums;
import com.kyf.mp.javaserver.modules.songmodule.mapper.AlbumsMapper;

/**
 * 专辑数据访问实现。
 */
@Service
public class AlbumsBusinessImpl extends BaseBusinessImpl<AlbumsMapper, Albums> implements IAlbumsBusiness {
}
