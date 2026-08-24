package com.kyf.mp.server.modules.queue.vo;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class ReturnQueue {
    private Long queueId;
    private String queueName;
    private Long creatorId;
    private Integer songCount;
    private Boolean isCurrent;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private List<QueueItemVO> queueItems;
}
