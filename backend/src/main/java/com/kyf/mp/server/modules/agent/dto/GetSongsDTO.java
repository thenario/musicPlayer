package com.kyf.mp.server.modules.agent.dto;

import lombok.Data;

@Data
public class GetSongsDTO {
    private Long id;
    private String songTitle;
    private String artist;
    private Long bitrate;
}
