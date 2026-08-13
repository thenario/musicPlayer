package com.kyf.mp.server.modules.playlist.entity;

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
@TableName("songs_playlists_relation")
public class SongsPlaylistsRelation implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long playlistId;

    private Long songId;

    private Integer songPlaylistPosition;
}
