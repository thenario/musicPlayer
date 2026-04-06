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
public class Songs implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "song_id", type = IdType.AUTO)
    private Integer songId;

    private String songTitle;

    private String artist;

    private String album;

    private String fileSize;

    private Integer uploaderId;

    private String uploaderName;

    private Integer duration;

    private Integer bitrate;

    private String songCoverUrl;

    private String songUrl;

    private LocalDateTime dateAdded;

    private String fileFormat;

    private String lyrics;

    /**
     * 翻译歌词(LRC格式)
     */
    private String tLyrics;
}
