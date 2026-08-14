package com.kyf.mp.server.modules.queue.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SetPlayModeDTO {
    @JsonProperty("play_mode")
    @NotBlank(message = "缺少必要参数")
    @Pattern(regexp = "sequential|repeat_all|repeat_one|shuffle", message = "播放模式不合法")
    private String playMode;
}
