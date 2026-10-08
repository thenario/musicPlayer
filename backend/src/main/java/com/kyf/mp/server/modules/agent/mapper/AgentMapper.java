package com.kyf.mp.server.modules.agent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.kyf.mp.server.modules.agent.dto.GetPlaylistsDTO;
import com.kyf.mp.server.modules.agent.dto.GetSongsDTO;
import com.kyf.mp.server.modules.agent.dto.GetUsersDTO;
import com.kyf.mp.server.modules.playlist.entity.Playlists;
import com.kyf.mp.server.modules.song.entity.Songs;
import com.kyf.mp.server.modules.user.entity.Users;

import org.apache.ibatis.annotations.Param;

public class AgentMapper {
    public interface InnerSongMapper extends BaseMapper<Songs> {
        IPage<GetSongsDTO> getSongs(@Param("keyword") String keyword, @Param("page") IPage<GetSongsDTO> page);
    }

    public interface InnerUserMapper extends BaseMapper<Users> {
        IPage<GetUsersDTO> getUsers(@Param("keyword") String keyword, @Param("page") IPage<GetUsersDTO> page);
    }

    public interface InnerPlaylistMapper extends BaseMapper<Playlists> {
        IPage<GetPlaylistsDTO> getPlaylists(@Param("keyword") String keyword, @Param("page") IPage<GetPlaylistsDTO> page);
    }
}
