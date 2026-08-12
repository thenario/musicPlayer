package com.kyf.mp.javaserver.modules.queuemodule.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class ReorderDTO {
    @JsonProperty("song_ids")
    @NotEmpty(message = "无效的歌曲列表")
    private List<Integer> songIds;
}
