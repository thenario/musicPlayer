package com.kyf.mp.server.modules.playlist.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import com.kyf.mp.server.modules.playlist.entity.UsersLikeplaylistsRelation;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author kyf
 * @since 2026-04-05
 */
public interface UsersLikeplaylistsRelationMapper extends BaseMapper<UsersLikeplaylistsRelation> {

    @Insert("INSERT IGNORE INTO users_likeplaylists_relation (user_id, playlist_id) "
            + "VALUES (#{userId}, #{playlistId})")
    int insertIgnore(UsersLikeplaylistsRelation relation);
}
