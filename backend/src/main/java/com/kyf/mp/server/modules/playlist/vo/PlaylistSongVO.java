package com.kyf.mp.server.modules.playlist.vo;

import com.kyf.mp.server.modules.song.entity.Songs;

import lombok.Data;

@Data
public class PlaylistSongVO extends Songs {
    private Integer songPlaylistPosition;
}
