package com.kyf.mp.server.modules.agent.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/agent")
public class AgentController {
    @GetMapping("/songs")
    public String getSongs(@RequestParam String keyword,
            @RequestParam String page) {
        return new String();
    }

    @GetMapping("/users")
    public String getUsers(@RequestParam String keyword,
            @RequestParam String page) {
        return new String();
    }

    @GetMapping("/playlists")
    public String getPlaylists(@RequestParam String keyword,
            @RequestParam String page) {
        return new String();
    }

}
