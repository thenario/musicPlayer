package com.kyf.mp.server.modules.song.repository.impl;

import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kyf.mp.server.common.repository.BaseRepositoryImpl;
import com.kyf.mp.server.modules.song.entity.Songs;
import com.kyf.mp.server.modules.song.mapper.SongsMapper;
import com.kyf.mp.server.modules.song.repository.SongsRepository;

/** Songs 数据访问实现。 */
@Repository
public class SongsRepositoryImpl extends BaseRepositoryImpl<SongsMapper, Songs> implements SongsRepository {

    public SongsRepositoryImpl(SongsMapper mapper) {
        super(mapper);
    }

    @Override
    public Page<Songs> findPage(Page<Songs> page, String keyword) {
        LambdaQueryWrapper<Songs> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(Songs.class, info -> !info.getColumn().equalsIgnoreCase("lyrics") &&
                !info.getColumn().equalsIgnoreCase("t_lyrics"));
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Songs::getSongTitle, keyword);
        }
        wrapper.orderByDesc(Songs::getDateAdded);
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public Songs findLyrics(Long songId) {
        return baseMapper.selectOne(new LambdaQueryWrapper<Songs>()
                .select(Songs::getLyrics, Songs::getTLyrics)
                .eq(Songs::getSongId, songId));
    }

    @Override
    public Page<Songs> findUploads(Long userId, Page<Songs> page) {
        return baseMapper.selectPage(page, new LambdaQueryWrapper<Songs>()
                .eq(Songs::getUploaderId, userId)
                .orderByDesc(Songs::getDateAdded));
    }

    @Override
    public Songs findUpload(Long userId, Long songId) {
        return baseMapper.selectOne(new LambdaQueryWrapper<Songs>()
                .eq(Songs::getSongId, songId)
                .eq(Songs::getUploaderId, userId));
    }

    @Override
    public List<Songs> findPlaylistSongs(List<Long> songIds) {
        return baseMapper.selectList(new LambdaQueryWrapper<Songs>()
                .select(Songs::getSongId, Songs::getSongTitle, Songs::getArtist, Songs::getAlbum,
                        Songs::getDuration, Songs::getBitrate, Songs::getSongCoverUrl,
                        Songs::getSongUrl, Songs::getFileFormat, Songs::getFileSize)
                .in(Songs::getSongId, songIds));
    }

    @Override
    public List<Songs> findScannerSongs(Long uploaderId) {
        return baseMapper.selectList(new QueryWrapper<Songs>()
                .select("song_id", "song_url").eq("uploader_id", uploaderId));
    }
}
