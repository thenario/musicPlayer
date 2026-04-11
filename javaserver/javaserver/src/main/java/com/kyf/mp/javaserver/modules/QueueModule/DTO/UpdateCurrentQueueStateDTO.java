package com.kyf.mp.javaserver.modules.QueueModule.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class UpdateCurrentQueueStateDTO {
    @JsonProperty("current_queue_id")
    private Integer currentQueueId;

    @JsonProperty("current_song_id")
    private Integer currentSongId;

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
