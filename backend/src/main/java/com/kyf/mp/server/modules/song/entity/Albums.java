package com.kyf.mp.server.modules.song.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 专辑
 * </p>
 *
 * @author kyf
 * @since 2026-08-12
 */
@Getter
@Setter
@TableName("albums")
public class Albums implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "album_id", type = IdType.ASSIGN_ID)
    private Long albumId;

    private String albumName;

    private String artistName;

    private String coverUrl;

    private LocalDate releaseDate;

    private String description;

    private LocalDateTime createdDate;
}
