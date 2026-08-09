package com.kyf.mp.javaserver.modules.PlaylistModule.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kyf.mp.javaserver.modules.PlaylistModule.entity.Playlists;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author kyf
 * @since 2026-04-05
 */
public interface PlaylistsMapper extends BaseMapper<Playlists> {
    int insertAndGetId(Playlists playlist);
}
