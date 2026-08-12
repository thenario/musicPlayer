package com.kyf.mp.javaserver.modules.queuemodule.vo;

import lombok.Data;

@Data
public class AddSongToQueueVO {
    private Long queueId;
    private Integer queueItemPosition;
    private Long queueItemId;
}
