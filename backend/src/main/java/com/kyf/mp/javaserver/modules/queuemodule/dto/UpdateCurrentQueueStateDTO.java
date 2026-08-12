package com.kyf.mp.javaserver.modules.queuemodule.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateCurrentQueueStateDTO {
    @JsonProperty("current_queue_id")
    @NotNull(message = "缺少队列ID")
    private Long currentQueueId;

    @JsonProperty("current_song_id")
    private Long currentSongId;

    @JsonProperty("current_position")
    private Integer currentPosition;

    @JsonProperty("current_progress")
    private Integer currentProgress;

    @JsonProperty("is_playing")
    private Boolean isPlaying;

    @JsonProperty("playmode")
    private String playmode;

    @JsonProperty("updated_date")
    private String updatedDate;
}
