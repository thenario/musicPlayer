package com.kyf.mp.javaserver.modules.PlaylistModule.VO;

import com.kyf.mp.javaserver.modules.SongModule.entity.Songs;

import lombok.Data;

@Data
public class PlaylistSongVO extends Songs {
    private Integer songPlaylistPosition;
}
