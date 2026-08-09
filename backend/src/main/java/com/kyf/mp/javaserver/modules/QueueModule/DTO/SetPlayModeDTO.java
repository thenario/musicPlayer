package com.kyf.mp.javaserver.modules.QueueModule.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class SetPlayModeDTO {
    @JsonProperty("play_mode")
    private String playMode;
}
