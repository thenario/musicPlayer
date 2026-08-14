package com.kyf.mp.server.modules.playlist.mapper;

import com.kyf.mp.server.modules.playlist.entity.SongsPlaylistsRelation;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/** 歌曲与歌单关系操作。复合主键不适用 MyBatis-Plus 的按 ID CRUD。 */
public interface SongsPlaylistsRelationMapper {

    @Select("SELECT playlist_id, song_id, song_playlist_position FROM songs_playlists_relation "
            + "WHERE playlist_id = #{playlistId}")
    List<SongsPlaylistsRelation> findByPlaylistId(@Param("playlistId") Long playlistId);

    @Select("SELECT playlist_id, song_id, song_playlist_position FROM songs_playlists_relation "
            + "WHERE playlist_id = #{playlistId} AND song_id = #{songId}")
    SongsPlaylistsRelation findByPlaylistAndSong(@Param("playlistId") Long playlistId, @Param("songId") Long songId);

    @Select("SELECT IFNULL(MAX(song_playlist_position), 0) FROM songs_playlists_relation "
            + "WHERE playlist_id = #{playlistId}")
    Integer getMaxPosition(@Param("playlistId") Long playlistId);

    @Insert("INSERT INTO songs_playlists_relation (playlist_id, song_id, song_playlist_position) "
            + "VALUES (#{playlistId}, #{songId}, #{songPlaylistPosition})")
    int insert(SongsPlaylistsRelation relation);

    @Delete("DELETE FROM songs_playlists_relation WHERE playlist_id = #{playlistId} AND song_id = #{songId}")
    int deleteByPlaylistAndSong(@Param("playlistId") Long playlistId, @Param("songId") Long songId);

    @Update("UPDATE songs_playlists_relation SET song_playlist_position = song_playlist_position - 1 "
            + "WHERE playlist_id = #{playlistId} AND song_playlist_position > #{position}")
    int decrementPositionsAfter(@Param("playlistId") Long playlistId, @Param("position") Integer position);
}