package com.kyf.mp.server.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.kyf.mp.server.modules.queue.service.QueuesService;
import com.kyf.mp.server.modules.queue.vo.CurrentQueueVO;
import com.kyf.mp.server.modules.song.service.SongsService;
import com.kyf.mp.server.modules.song.vo.LyricsVO;
import com.kyf.mp.server.modules.user.service.UsersService;
import com.kyf.mp.server.modules.user.vo.LoginVO;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class ApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsersService usersService;
    @MockBean
    private QueuesService queuesService;
    @MockBean
    private SongsService songsService;

    @Test
    @DisplayName("登录接口应校验请求并包装服务层结果")
    void loginEndpointReturnsSuccessEnvelope() throws Exception {
        when(usersService.login("alice", "secret")).thenReturn(new LoginVO());

        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"user_name\":\"alice\",\"password\":\"secret\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists());

        verify(usersService).login("alice", "secret");
    }

    @Test
    @DisplayName("当前队列接口应读取请求用户并调用队列服务")
    void currentQueueEndpointPassesUserId() throws Exception {
        when(queuesService.getCurrentQueue(7L)).thenReturn(new CurrentQueueVO());

        mockMvc.perform(get("/api/queues/current").requestAttr("userId", 7L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(queuesService).getCurrentQueue(7L);
    }

    @Test
    @DisplayName("歌词接口应返回服务层歌词数据")
    void lyricsEndpointReturnsLyrics() throws Exception {
        LyricsVO lyrics = new LyricsVO();
        lyrics.setLyrics("[00:01.00]hello");
        when(songsService.getLyrics(3L)).thenReturn(lyrics);

        mockMvc.perform(get("/api/songs/3/lyrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.lyrics").value("[00:01.00]hello"));

        verify(songsService).getLyrics(3L);
    }

    @Test
    @DisplayName("播放模式接口应拒绝非法模式且不调用服务层")
    void playModeEndpointValidatesRequest() throws Exception {
        mockMvc.perform(patch("/api/queues/2/play-mode")
                .requestAttr("userId", 7L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"play_mode\":\"invalid\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));

        verify(queuesService, never()).setPlayMode(any(), any(), any());
    }
}
