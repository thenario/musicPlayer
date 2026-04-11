package com.kyf.mp.javaserver.modules.QueueModule.VO;

import com.kyf.mp.javaserver.modules.QueueModule.entity.PlayState;

import lombok.Data;

@Data
public class CurrentQueue {
    private PlayState queueState;
    private ReturnQueue queue;
}
