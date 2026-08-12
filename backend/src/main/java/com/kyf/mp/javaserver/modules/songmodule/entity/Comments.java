package com.kyf.mp.javaserver.modules.songmodule.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 评论（支持回复）
 * </p>
 *
 * @author kyf
 * @since 2026-08-12
 */
@Getter
@Setter
@TableName("comments")
public class Comments implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "comment_id", type = IdType.ASSIGN_ID)
    private Long commentId;

    private Long userId;

    private String targetType;

    private Long targetId;

    private Long parentId;

    private String content;

    private LocalDateTime createdDate;
}
