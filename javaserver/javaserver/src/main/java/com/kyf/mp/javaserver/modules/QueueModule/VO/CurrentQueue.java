package com.kyf.mp.javaserver.modules.QueueModule.VO;

import lombok.Data;

@Data
public class CurrentQueue {
    private QueueStateVO queueState;
    private ReturnQueue queue;
}
