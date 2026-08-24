package com.kyf.mp.server.modules.playlist.vo;

import java.io.Serializable;
import java.util.List;

import com.kyf.mp.server.modules.playlist.entity.Playlists;

import lombok.Data;

/**
 * 不随当前用户变化的歌单详情内容，可按歌单 ID 缓存。
 */
@Data
public class PlaylistContentVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Playlists playlist;
    private List<PlaylistSongVO> songs;
}
