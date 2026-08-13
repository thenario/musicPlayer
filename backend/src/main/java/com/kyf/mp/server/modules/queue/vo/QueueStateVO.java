package com.kyf.mp.server.modules.queue.vo;

import lombok.Data;

@Data
public class QueueStateVO {
    private Long currentQueueId;
    private Long currentSongId;
    private Integer currentPosition;
    private Integer currentProgress;
    private String playmode;
}
