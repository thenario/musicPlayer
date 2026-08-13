package com.kyf.mp.server.modules.queue.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateQueueFromPlaylistDTO {
    @JsonProperty("playlist_id")
    @NotNull(message = "歌单ID不能为空")
    @Min(value = 1, message = "歌单ID必须合法")
    private Long playlistId;
}
