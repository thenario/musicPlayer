package com.kyf.mp.javaserver.modules.queuemodule.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AlterQueueDTO {
    @JsonProperty("queue_id")
    @NotNull(message = "缺少 queue_id")
    private Integer queueId;
}
