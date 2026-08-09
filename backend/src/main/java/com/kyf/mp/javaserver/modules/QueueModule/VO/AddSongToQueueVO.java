package com.kyf.mp.javaserver.modules.QueueModule.VO;

import lombok.Data;

@Data
public class AddSongToQueueVO {
    private Integer queueId;
    private Integer queueItemPosition;
    private Integer queueItemId;
}
