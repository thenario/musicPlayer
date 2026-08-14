package com.kyf.mp.server.modules.playlist.mapper;

import com.kyf.mp.server.modules.playlist.entity.UsersPlaylistsRelation;
import org.apache.ibatis.annotations.Insert;

/** 用户创建歌单的关联写入。复合主键不适用 MyBatis-Plus 的按 ID CRUD。 */
public interface UsersPlaylistsRelationMapper {

    @Insert("INSERT INTO users_playlists_relation (user_id, playlist_id) VALUES (#{userId}, #{playlistId})")
    int insert(UsersPlaylistsRelation relation);
}