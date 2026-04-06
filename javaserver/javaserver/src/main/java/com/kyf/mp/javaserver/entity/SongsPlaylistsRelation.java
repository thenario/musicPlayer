package com.kyf.mp.javaserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
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

    private Integer playlistId;

    private Integer songId;

    private Integer songPlaylistPosition;
}
