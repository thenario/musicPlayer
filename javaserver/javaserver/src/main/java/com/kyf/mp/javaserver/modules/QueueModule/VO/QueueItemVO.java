package com.kyf.mp.javaserver.modules.QueueModule.VO;

import java.time.LocalDateTime;

import com.kyf.mp.javaserver.modules.SongModule.VO.SongVO;

import lombok.Data;

@Data
public class QueueItemVO {
    private Integer queueItemId;
    private Integer queueItemPosition;
    private Integer queueId;
    private LocalDateTime addedDate;

    private SongVO song;

}
