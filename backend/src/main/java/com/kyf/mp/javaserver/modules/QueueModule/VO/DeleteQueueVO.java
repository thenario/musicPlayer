package com.kyf.mp.javaserver.modules.QueueModule.VO;

import lombok.Data;

@Data
public class DeleteQueueVO {
    private boolean wasActive;
    private Integer newQueueId;
}
