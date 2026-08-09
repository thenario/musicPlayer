package com.kyf.mp.javaserver.modules.queuemodule.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class UpdateQueueStateDataDTO {
    @JsonProperty("stateData")
    private UpdateCurrentQueueStateDTO stateData;
}
