package com.kyf.mp.server.modules.queue.vo;

import lombok.Data;

@Data
public class CurrentQueue {
    private QueueStateVO queueState;
    private ReturnQueue queue;
}
