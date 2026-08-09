package com.kyf.mp.javaserver.modules.queuemodule.vo;

import lombok.Data;

@Data
public class AddSongToQueueVO {
    private Integer queueId;
    private Integer queueItemPosition;
    private Integer queueItemId;
}
