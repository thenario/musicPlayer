package com.kyf.mp.javaserver.modules.playlistmodule.vo;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class PlaylistSummaryVO {

    private Long playlistId;
    private String playlistName;
    private String playlistCoverUrl;
    private Integer songCount;
    private Integer playCount;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private String createdDate;

}
