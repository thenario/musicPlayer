package com.kyf.mp.javaserver.modules.QueueModule.DTO;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ReorderDTO {
    @JsonProperty("song_ids")
    private List<Integer> songIds;
}
