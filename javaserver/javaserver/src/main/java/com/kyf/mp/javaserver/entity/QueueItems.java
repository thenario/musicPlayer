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
@TableName("queue_items")
public class QueueItems implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "queue_item_id", type = IdType.AUTO)
    private Integer queueItemId;

    private Integer queueId;

    private Integer songId;

    private Integer queueItemPosition;

    private LocalDateTime addedDate;
}
