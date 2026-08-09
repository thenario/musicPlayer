package com.kyf.mp.javaserver.modules.playlistmodule.vo;

import com.kyf.mp.javaserver.modules.songmodule.entity.Songs;

import lombok.Data;

@Data
public class PlaylistSongVO extends Songs {
    private Integer songPlaylistPosition;
}
