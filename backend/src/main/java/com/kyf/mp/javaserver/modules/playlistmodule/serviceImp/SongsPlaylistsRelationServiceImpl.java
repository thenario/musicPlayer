package com.kyf.mp.javaserver.modules.playlistmodule.serviceImp;

import com.kyf.mp.javaserver.modules.playlistmodule.entity.SongsPlaylistsRelation;
import com.kyf.mp.javaserver.modules.playlistmodule.mapper.SongsPlaylistsRelationMapper;
import com.kyf.mp.javaserver.modules.songmodule.service.ISongsPlaylistsRelationService;
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
