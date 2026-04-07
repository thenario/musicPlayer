package com.kyf.mp.javaserver.modelVO;

import java.util.List;

import com.kyf.mp.javaserver.entity.Playlists;
import com.kyf.mp.javaserver.entity.Songs;

import lombok.Data;

@Data
public class PlaylistDetailVO {
    private Playlists playlist;
    private List<PlaylistSongVO> songs;// 此处的song是不包括lyrics的
    private Boolean isLiked;
}
