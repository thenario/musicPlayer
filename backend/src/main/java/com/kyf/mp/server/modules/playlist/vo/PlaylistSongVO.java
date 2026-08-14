package com.kyf.mp.server.modules.playlist.vo;

import com.kyf.mp.server.modules.song.entity.Songs;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class PlaylistSongVO extends Songs {
    private Integer songPlaylistPosition;
}
