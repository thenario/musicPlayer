package com.kyf.mp.server.modules.queue.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 *
 * </p>
 *
 * @author kyf
 * @since 2026-04-05
 */
@Getter
@Setter
@TableName("play_state")
public class PlayState implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "user_id", type = IdType.INPUT)
    private Long userId;

    private Long currentQueueId;

    private Long currentSongId;

    private Integer currentPosition;

    private Integer currentProgress;

    private String playmode;

    private LocalDateTime updatedDate;
}
