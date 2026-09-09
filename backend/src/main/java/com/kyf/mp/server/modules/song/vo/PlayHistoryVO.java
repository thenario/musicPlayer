package com.kyf.mp.server.modules.song.vo;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class PlayHistoryVO {
    private LocalDateTime playTime;
    private SongVO song;
}
