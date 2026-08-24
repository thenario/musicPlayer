package com.kyf.mp.server.modules.playlist.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kyf.mp.server.modules.playlist.entity.Playlists;
import lombok.Data;

@Data
public class PlaylistDetailVO {
    private Playlists playlist;
    private List<PlaylistSongVO> songs;// 此处的song是不包括lyrics的
    @JsonProperty("is_liked")
    private Boolean liked;
}
