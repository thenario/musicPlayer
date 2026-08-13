package com.kyf.mp.server.modules.playlist.vo;

import java.util.List;

import com.kyf.mp.server.modules.playlist.entity.Playlists;
import com.kyf.mp.server.modules.song.entity.Songs;

import lombok.Data;

@Data
public class PlaylistDetailVO {
    private Playlists playlist;
    private List<PlaylistSongVO> songs;// 此处的song是不包括lyrics的
    private Boolean isLiked;
}
