package com.kyf.mp.javaserver.modules.QueueModule.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class AddSongToQueue {
    @JsonProperty("song_id")
    private Integer songId;
    @JsonProperty("mode")
    private Boolean mode;
}
