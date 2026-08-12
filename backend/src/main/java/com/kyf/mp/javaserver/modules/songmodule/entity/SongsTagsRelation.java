package com.kyf.mp.javaserver.modules.songmodule.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 歌曲-标签关联（多对多）
 * </p>
 *
 * @author kyf
 * @since 2026-08-12
 */
@Getter
@Setter
@TableName("songs_tags_relation")
public class SongsTagsRelation implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long songId;

    private Long tagId;
}
