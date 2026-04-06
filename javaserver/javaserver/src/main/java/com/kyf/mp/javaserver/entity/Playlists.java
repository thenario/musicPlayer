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
public class Playlists implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "playlist_id", type = IdType.AUTO)
    private Integer playlistId;

    private String playlistName;

    private Integer creatorId;

    private LocalDateTime createdDate;

    private LocalDateTime updatedDate;

    private String playlistCoverUrl;

    private Integer songCount;

    private Integer likeCount;

    private Integer playCount;

    private Boolean isPublic;

    private String description;
}
