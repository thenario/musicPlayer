package com.kyf.mp.server.modules.song.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 歌曲标签字典
 * </p>
 *
 * @author kyf
 * @since 2026-08-12
 */
@Getter
@Setter
@TableName("tags")
public class Tags implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "tag_id", type = IdType.ASSIGN_ID)
    private Long tagId;

    private String tagName;

    private String tagType;

    private LocalDateTime createdDate;
}
