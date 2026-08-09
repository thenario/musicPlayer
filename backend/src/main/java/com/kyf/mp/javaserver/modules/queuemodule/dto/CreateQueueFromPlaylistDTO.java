package com.kyf.mp.javaserver.modules.queuemodule.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class CreateQueueFromPlaylistDTO {
    @JsonProperty("playlist_id")
    private Integer playlistId;
}
