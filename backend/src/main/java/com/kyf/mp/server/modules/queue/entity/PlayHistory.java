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
 * 播放历史
 * </p>
 *
 * @author kyf
 * @since 2026-08-12
 */
@Getter
@Setter
@TableName("play_history")
public class PlayHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "history_id", type = IdType.ASSIGN_ID)
    private Long historyId;

    private Long userId;

    private Long songId;

    private LocalDateTime playedDate;
}
