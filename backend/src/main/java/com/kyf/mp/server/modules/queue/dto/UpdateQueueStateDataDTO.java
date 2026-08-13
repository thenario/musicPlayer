package com.kyf.mp.server.modules.queue.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateQueueStateDataDTO {
    @JsonProperty("stateData")
    @NotNull(message = "缺少播放状态数据")
    private UpdateCurrentQueueStateDTO stateData;
}
