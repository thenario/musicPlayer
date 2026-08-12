package com.kyf.mp.javaserver.modules.queuemodule.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SetPlayModeDTO {
    @JsonProperty("play_mode")
    @NotBlank(message = "缺少必要参数")
    private String playMode;
}
