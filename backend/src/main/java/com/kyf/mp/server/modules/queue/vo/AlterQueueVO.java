package com.kyf.mp.server.modules.queue.vo;

import lombok.Data;

@Data
public class AlterQueueVO {
    private Long currentSongId;
    private Integer currentPosition;
}
