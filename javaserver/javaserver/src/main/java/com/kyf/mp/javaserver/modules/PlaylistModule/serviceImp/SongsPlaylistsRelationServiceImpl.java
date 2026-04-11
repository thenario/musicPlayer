package com.kyf.mp.javaserver.modules.PlaylistModule.serviceImp;

import com.kyf.mp.javaserver.modules.PlaylistModule.entity.SongsPlaylistsRelation;
import com.kyf.mp.javaserver.modules.PlaylistModule.mapper.SongsPlaylistsRelationMapper;
import com.kyf.mp.javaserver.modules.SongModule.service.ISongsPlaylistsRelationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author kyf
 * @since 2026-04-05
 */
@Service
public class SongsPlaylistsRelationServiceImpl extends ServiceImpl<SongsPlaylistsRelationMapper, SongsPlaylistsRelation>
        implements ISongsPlaylistsRelationService {

}
