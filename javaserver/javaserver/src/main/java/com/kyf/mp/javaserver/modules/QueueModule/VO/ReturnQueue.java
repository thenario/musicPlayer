package com.kyf.mp.javaserver.modules.QueueModule.VO;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class ReturnQueue {
    private Integer queueId;
    private String queueName;
    private Integer creatorId;
    private Integer songCount;
    private Boolean isCurrent;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    private List<QueueItemVO> queueItems;
}
