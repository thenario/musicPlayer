package com.kyf.mp.javaserver.modelVO;

import com.kyf.mp.javaserver.entity.Songs;

import lombok.Data;

@Data
public class PlaylistSongVO extends Songs {
    private Integer songPlaylistPosition;
}
