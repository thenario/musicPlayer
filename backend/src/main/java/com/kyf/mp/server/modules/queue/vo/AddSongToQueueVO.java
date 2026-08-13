package com.kyf.mp.server.modules.queue.vo;

import lombok.Data;

@Data
public class AddSongToQueueVO {
    private Long queueId;
    private Integer queueItemPosition;
    private Long queueItemId;
}
