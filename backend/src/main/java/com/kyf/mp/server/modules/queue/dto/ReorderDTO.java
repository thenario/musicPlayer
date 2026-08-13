package com.kyf.mp.server.modules.queue.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class ReorderDTO {
    @JsonProperty("song_ids")
    @NotEmpty(message = "无效的歌曲列表")
    private List<Long> songIds;
}
