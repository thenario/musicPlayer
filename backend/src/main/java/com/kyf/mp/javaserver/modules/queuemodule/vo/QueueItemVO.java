package com.kyf.mp.javaserver.modules.queuemodule.vo;

import java.time.LocalDateTime;

import com.kyf.mp.javaserver.modules.songmodule.vo.SongVO;

import lombok.Data;

@Data
public class QueueItemVO {
    private Long queueItemId;
    private Integer queueItemPosition;
    private Long queueId;
    private LocalDateTime addedDate;

    private SongVO song;

}
