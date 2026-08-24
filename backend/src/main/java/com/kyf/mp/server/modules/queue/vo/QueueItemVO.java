package com.kyf.mp.server.modules.queue.vo;

import java.time.LocalDateTime;

import com.kyf.mp.server.modules.song.vo.SongVO;

import lombok.Data;

@Data
public class QueueItemVO {
    private Long queueItemId;
    private Integer queueItemPosition;
    private Long queueId;
    private LocalDateTime addedDate;
    private SongVO song;
}
