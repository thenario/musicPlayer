package com.kyf.mp.server.modules.agent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.kyf.mp.server.modules.playlist.entity.Playlists;
import com.kyf.mp.server.modules.song.entity.Songs;
import com.kyf.mp.server.modules.user.entity.Users;

import org.apache.ibatis.annotations.Param;
public class AgentMapper {
    public interface InnerSongMapper extends BaseMapper<Songs> {
        IPage<Songs> getSongs(@Param("keyword") String keyword, @Param("page") IPage<Songs> page);
    }

    public interface InnerUserMapper extends BaseMapper<Users> {
        IPage<Users> getUsers(@Param("keyword") String keyword, @Param("page") IPage<Users> page);
    }

    public interface InnerPlaylist extends BaseMapper<Playlists> {
        IPage<Playlists> getSongs(@Param("keyword") String keyword, @Param("page") IPage<Playlists> page);
    }
}