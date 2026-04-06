package com.kyf.mp.javaserver.entity;

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

    @TableId(value = "user_id", type = IdType.AUTO)
    private Integer userId;

    private Integer currentQueueId;

    private Integer currentSongId;

    private Integer currentPosition;

    private Integer currentProgress;

    private String playmode;

    private LocalDateTime updatedDate;
}
