package com.kyf.mp.javaserver.modules.queuemodule.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class AlterQueueDTO {
    @JsonProperty("queue_id")
    private Integer queueId;
}
