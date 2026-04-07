package com.kyf.mp.javaserver.modelVO;

import java.util.List;

import lombok.Data;

@Data
public class MyPlaylistsVO {

    private List<PlaylistSummaryVO> playlists;
}
