package com.kyf.mp.javaserver.modules.PlaylistModule.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 
 * </p>
 *
 * @author kyf
 * @since 2026-04-05
 */
@Getter
@Setter
@TableName("users_likeplaylists_relation")
public class UsersLikeplaylistsRelation implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer userId;

    private Integer playlistId;
}
