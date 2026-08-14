package com.kyf.mp.server.modules.queue.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AlterQueueDTO {
    @JsonProperty("queue_id")
    @NotNull(message = "缺少 queue_id")
    @Min(value = 1, message = "queue_id 必须合法")
    private Long queueId;
}
