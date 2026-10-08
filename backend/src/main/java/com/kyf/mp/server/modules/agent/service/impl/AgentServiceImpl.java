package com.kyf.mp.server.modules.agent.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kyf.mp.server.modules.agent.mapper.AgentMapper;
import com.kyf.mp.server.modules.agent.service.AgentService;
import com.kyf.mp.server.modules.agent.dto.GetPlaylistsDTO;
import com.kyf.mp.server.modules.agent.dto.GetSongsDTO;
import com.kyf.mp.server.modules.agent.dto.GetUsersDTO;

@Service 
public class AgentServiceImpl implements AgentService {
    private final AgentMapper.InnerPlaylistMapper playlistMapper;
    private final AgentMapper.InnerUserMapper userMapper;
    private final AgentMapper.InnerSongMapper songMapper;

    public AgentServiceImpl(AgentMapper.InnerPlaylistMapper playlistMapper, AgentMapper.InnerSongMapper songMapper,
            AgentMapper.InnerUserMapper userMapper) {
        this.playlistMapper = playlistMapper;
        this.userMapper = userMapper;
        this.songMapper = songMapper;
    }

    @Override
    public IPage<GetPlaylistsDTO> getPlaylists(String keyword, String page) {
        return playlistMapper.getPlaylists(keyword, new Page<GetPlaylistsDTO>(Long.parseLong(page), 10L));
    }

    @Override
    public IPage<GetUsersDTO> getUsers(String keyword, String page) {
        return userMapper.getUsers(keyword, new Page<GetUsersDTO>(Long.parseLong(page), 10L));
    }

    @Override
    public IPage<GetSongsDTO> geSongs(String keyword, String page) {
        return songMapper.getSongs(keyword, new Page<GetSongsDTO>(Long.parseLong(page), 10L));
    }
}
