package com.kyf.mp.server.modules.agent.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.kyf.mp.server.modules.agent.dto.GetPlaylistsDTO;
import com.kyf.mp.server.modules.agent.dto.GetSongsDTO;
import com.kyf.mp.server.modules.agent.dto.GetUsersDTO;

public interface AgentService {
    IPage<GetUsersDTO> getUsers(String keyword, String page);
    IPage<GetSongsDTO> geSongs(String keyword, String page);
    IPage<GetPlaylistsDTO> getPlaylists(String keyword, String page);
}
