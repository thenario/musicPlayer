package com.kyf.mp.javaserver.modules.PlaylistModule.VO;

import java.util.List;

import lombok.Data;

@Data
public class MyPlaylistsVO {

    private List<PlaylistSummaryVO> playlists;
}
