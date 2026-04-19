package com.kyf.mp.javaserver.modules.SongModule.VO;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class UploadsVO {
    private String songTitle;
    private String artist;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime dateAdded;
    private String songCoverUrl;
    private String songUrl;
    private Integer songId;
}
