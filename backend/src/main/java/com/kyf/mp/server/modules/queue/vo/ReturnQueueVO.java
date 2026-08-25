package com.kyf.mp.server.modules.queue.vo;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ReturnQueueVO {
    private Long queueId;
    private String queueName;
    private Long creatorId;
    private Integer songCount;
    @JsonProperty("is_current")
    private Boolean current;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private List<QueueItemVO> queueItems;
}
