package com.kyf.mp.server.modules.queue.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateCurrentQueueStateDTO {
    @JsonProperty("current_queue_id")
    @NotNull(message = "缺少队列ID")
    @Min(value = 1, message = "队列ID必须合法")
    private Long currentQueueId;

    @JsonProperty("current_song_id")
    @Min(value = 1, message = "歌曲ID必须合法")
    private Long currentSongId;

    @JsonProperty("current_position")
    @Min(value = 0, message = "播放位置不能为负数")
    private Integer currentPosition;

    @JsonProperty("current_progress")
    @Min(value = 0, message = "播放进度不能为负数")
    private Integer currentProgress;

    @JsonProperty("is_playing")
    private Boolean isPlaying;

    @JsonProperty("playmode")
    @Pattern(regexp = "sequential|repeat_all|repeat_one|shuffle", message = "播放模式不合法")
    private String playmode;

    @JsonProperty("updated_date")
    private String updatedDate;
}
