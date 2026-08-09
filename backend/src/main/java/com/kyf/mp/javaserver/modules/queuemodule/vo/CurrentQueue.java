package com.kyf.mp.javaserver.modules.queuemodule.vo;

import lombok.Data;

@Data
public class CurrentQueue {
    private QueueStateVO queueState;
    private ReturnQueue queue;
}
