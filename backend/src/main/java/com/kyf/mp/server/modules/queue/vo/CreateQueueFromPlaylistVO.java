package com.kyf.mp.server.modules.queue.vo;

import lombok.Data;

@Data
public class CreateQueueFromPlaylistVO {
    private Integer songCount;
    private Long queueId;
}
