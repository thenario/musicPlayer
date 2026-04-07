package com.kyf.mp.javaserver.mapper;

import com.kyf.mp.javaserver.entity.SongsPlaylistsRelation;

import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author kyf
 * @since 2026-04-05
 */
public interface SongsPlaylistsRelationMapper extends BaseMapper<SongsPlaylistsRelation> {
    @Select("SELECT IFNULL(MAX(song_playlist_position), 0) FROM songs_playlists_relation WHERE playlist_id = #{playlistId}")
    Integer getMaxPosition(Integer playlistId);
}
