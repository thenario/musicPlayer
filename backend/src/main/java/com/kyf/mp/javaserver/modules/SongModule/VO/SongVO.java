package com.kyf.mp.javaserver.modules.SongModule.VO;

import lombok.Data;

@Data
public class SongVO {
    private Integer songId;
    private String songTitle;
    private String songCoverUrl;
    private String songUrl;
    private String artist;
    private String album;
}
