package com.kyf.mp.server.modules.agent.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.kyf.mp.server.modules.agent.dto.GetPlaylistsDTO;
import com.kyf.mp.server.modules.agent.dto.GetSongsDTO;
import com.kyf.mp.server.modules.agent.dto.GetUsersDTO;
import com.kyf.mp.server.modules.agent.service.AgentService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/agent")
public class AgentController {
    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    @GetMapping("/songs")
    public IPage<GetSongsDTO> getSongs(@RequestParam String keyword,
            @RequestParam String page) {
        return agentService.geSongs(keyword, page);
    }

    @GetMapping("/users")
    public IPage<GetUsersDTO> getUsers(@RequestParam String keyword,
            @RequestParam String page) {
        return agentService.getUsers(keyword, page);
    }

    @GetMapping("/playlists")
    public IPage<GetPlaylistsDTO> getPlaylists(@RequestParam String keyword,
            @RequestParam String page) {
        return agentService.getPlaylists(keyword, page);
    }

}
