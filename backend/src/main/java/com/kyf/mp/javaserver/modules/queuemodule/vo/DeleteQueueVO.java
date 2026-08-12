package com.kyf.mp.javaserver.modules.queuemodule.vo;

import lombok.Data;

@Data
public class DeleteQueueVO {
    private boolean wasActive;
    private Long newQueueId;
}
