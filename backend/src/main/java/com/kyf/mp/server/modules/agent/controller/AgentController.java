package com.kyf.mp.server.modules.agent.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.kyf.mp.server.common.ResultModel;
import com.kyf.mp.server.modules.agent.dto.GetPlaylistsDTO;
import com.kyf.mp.server.modules.agent.dto.GetSongsDTO;
import com.kyf.mp.server.modules.agent.dto.GetUsersDTO;
import com.kyf.mp.server.modules.agent.service.AgentService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/api/agent")
public class AgentController {
    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    @GetMapping("/songs")
    public ResultModel<IPage<GetSongsDTO>> getSongs(@RequestParam String keyword,
            @RequestParam @Min(value = 1, message = "页码必须大于等于 1") Long page) {
        return ResultModel.success(agentService.geSongs(keyword, page));
    }

    @GetMapping("/users")
    public ResultModel<IPage<GetUsersDTO>> getUsers(@RequestParam String keyword,
            @RequestParam @Min(value = 1, message = "页码必须大于等于 1") Long page) {
        return ResultModel.success(agentService.getUsers(keyword, page));
    }

    @GetMapping("/playlists")
    public ResultModel<IPage<GetPlaylistsDTO>> getPlaylists(@RequestParam String keyword,
            @RequestParam @Min(value = 1, message = "页码必须大于等于 1") Long page) {
        return ResultModel.success(agentService.getPlaylists(keyword, page));
    }

}
