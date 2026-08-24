package com.kyf.mp.server.modules.playlist.mapper;

import com.kyf.mp.server.modules.playlist.entity.UsersLikeplaylistsRelation;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/** 歌单点赞关系操作。复合主键不适用 MyBatis-Plus 的按 ID CRUD。 */
public interface UsersLikeplaylistsRelationMapper {

        @Select("SELECT COUNT(*) FROM users_likeplaylists_relation "
                        + "WHERE user_id = #{userId} AND playlist_id = #{playlistId}")
        int countByUserAndPlaylist(@Param("userId") Long userId, @Param("playlistId") Long playlistId);

        @Insert("INSERT IGNORE INTO users_likeplaylists_relation (user_id, playlist_id) "
                        + "VALUES (#{userId}, #{playlistId})")
        int insertIgnore(UsersLikeplaylistsRelation relation);

        @Delete("DELETE FROM users_likeplaylists_relation WHERE user_id = #{userId} AND playlist_id = #{playlistId}")
        int deleteByUserAndPlaylist(@Param("userId") Long userId, @Param("playlistId") Long playlistId);
}