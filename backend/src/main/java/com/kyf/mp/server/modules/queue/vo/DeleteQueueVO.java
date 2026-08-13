package com.kyf.mp.server.modules.queue.vo;

import lombok.Data;

@Data
public class DeleteQueueVO {
    private boolean wasActive;
    private Long newQueueId;
}
