package com.kyf.mp.javaserver.modules.songmodule.vo;

import lombok.Data;

@Data
public class SongVO {
    private Long songId;
    private String songTitle;
    private String songCoverUrl;
    private String songUrl;
    private String artist;
    private String album;
}
