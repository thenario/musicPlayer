package com.kyf.mp.javaserver.modules.PlaylistModule.VO;

import java.util.List;

import com.kyf.mp.javaserver.modules.PlaylistModule.entity.Playlists;
import com.kyf.mp.javaserver.modules.SongModule.entity.Songs;

import lombok.Data;

@Data
public class PlaylistDetailVO {
    private Playlists playlist;
    private List<PlaylistSongVO> songs;// 此处的song是不包括lyrics的
    private Boolean isLiked;
}
