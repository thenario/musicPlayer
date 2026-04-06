package com.kyf.mp.javaserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
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

    @TableId(value = "queue_id", type = IdType.AUTO)
    private Integer queueId;

    private String queueName;

    private Integer creatorId;

    private Integer songCount;

    private Boolean isCurrent;

    private LocalDateTime createdDate;

    private LocalDateTime updatedDate;

    private LocalDateTime createdAt;
}
