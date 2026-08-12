package com.kyf.mp.javaserver.modules.queuemodule.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddSongToQueue {
    @JsonProperty("song_id")
    @NotNull(message = "歌曲ID不能为空")
    @Min(value = 1, message = "歌曲ID必须合法")
    private Integer songId;
    @JsonProperty("mode")
    private Boolean mode;
}
