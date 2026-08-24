package com.kyf.mp.server.modules.queue.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public class Queues implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "queue_id", type = IdType.ASSIGN_ID)
    private Long queueId;

    private String queueName;

    private Long creatorId;

    private Integer songCount;

    @TableField("is_current")
    @JsonProperty("is_current")
    private Boolean current;

    private LocalDateTime createdDate;

    private LocalDateTime updatedDate;
}
