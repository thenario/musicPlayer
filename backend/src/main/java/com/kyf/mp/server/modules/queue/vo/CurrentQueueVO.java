package com.kyf.mp.server.modules.queue.vo;

import lombok.Data;

@Data
public class CurrentQueueVO {
    private QueueStateVO queueState;
    private ReturnQueueVO queue;
}
