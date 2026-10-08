package com.kyf.mp.server.modules.agent.dto;

import lombok.Data;

@Data
public class GetPlaylistsDTO {
    private Long id;
    private String playlistName;
    private String description;
    private String createdDate;
}
