package com.kyf.mp.server.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kyf.mp.server.modules.agent.controller.AgentController;
import com.kyf.mp.server.modules.agent.dto.GetPlaylistsDTO;
import com.kyf.mp.server.modules.agent.dto.GetSongsDTO;
import com.kyf.mp.server.modules.agent.dto.GetUsersDTO;
import com.kyf.mp.server.modules.agent.mapper.AgentMapper;
import com.kyf.mp.server.modules.agent.service.impl.AgentServiceImpl;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("业务异常状态码合法时，应保留原状态码和消息")
    void preservesValidBusinessStatus() {
        ResponseEntity<ResultModel<Void>> response =
                handler.handleBusinessException(new BusinessException(403, "没有访问权限"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody())
                .extracting(ResultModel::getCode, ResultModel::getMessage)
                .containsExactly(403, "没有访问权限");
    }

    @Test
    @DisplayName("业务异常状态码非法时，应映射为 400")
    void mapsInvalidBusinessStatusToBadRequest() {
        ResponseEntity<ResultModel<Void>> response =
                handler.handleBusinessException(new BusinessException(999, "无效状态"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getCode()).isEqualTo(400);
    }

    @ParameterizedTest
    @CsvSource({
            "songs, abc, 请求参数类型不匹配",
            "users, abc, 请求参数类型不匹配",
            "playlists, abc, 请求参数类型不匹配",
            "songs, 9223372036854775808, 请求参数类型不匹配",
            "users, 9223372036854775808, 请求参数类型不匹配",
            "playlists, 9223372036854775808, 请求参数类型不匹配",
            "songs, 0, 页码必须大于等于 1",
            "users, 0, 页码必须大于等于 1",
            "playlists, 0, 页码必须大于等于 1",
            "songs, -1, 页码必须大于等于 1",
            "users, -1, 页码必须大于等于 1",
            "playlists, -1, 页码必须大于等于 1"
    })
    @DisplayName("Agent 查询的非法页码应返回 400，并且不调用 Mapper")
    void agentEndpointsRejectInvalidPage(String resource, String page, String message) throws Exception {
        var playlistMapper = mock(AgentMapper.InnerPlaylistMapper.class);
        var songMapper = mock(AgentMapper.InnerSongMapper.class);
        var userMapper = mock(AgentMapper.InnerUserMapper.class);
        var service = new AgentServiceImpl(playlistMapper, songMapper, userMapper);
        var mvc = MockMvcBuilders.standaloneSetup(new AgentController(service))
                .setControllerAdvice(handler)
                .build();

        mvc.perform(get("/api/agent/" + resource)
                .param("keyword", "青")
                .param("page", page))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(message));

        verifyNoInteractions(playlistMapper, songMapper, userMapper);
    }

    @ParameterizedTest
    @ValueSource(strings = { "songs", "users", "playlists" })
    @DisplayName("Agent 查询的合法页码应通过校验并传给对应 Mapper")
    void agentEndpointsAcceptValidPage(String resource) throws Exception {
        var playlistMapper = mock(AgentMapper.InnerPlaylistMapper.class);
        var songMapper = mock(AgentMapper.InnerSongMapper.class);
        var userMapper = mock(AgentMapper.InnerUserMapper.class);
        when(playlistMapper.getPlaylists(eq("青"), any())).thenReturn(new Page<GetPlaylistsDTO>(2, 10));
        when(songMapper.getSongs(eq("青"), any())).thenReturn(new Page<GetSongsDTO>(2, 10));
        when(userMapper.getUsers(eq("青"), any())).thenReturn(new Page<GetUsersDTO>(2, 10));
        var service = new AgentServiceImpl(playlistMapper, songMapper, userMapper);
        var mvc = MockMvcBuilders.standaloneSetup(new AgentController(service))
                .setControllerAdvice(handler)
                .build();

        mvc.perform(get("/api/agent/" + resource)
                .param("keyword", "青")
                .param("page", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.records").isArray());

        switch (resource) {
            case "songs" -> verify(songMapper).getSongs(eq("青"),
                    argThat(p -> p.getCurrent() == 2 && p.getSize() == 10));
            case "users" -> verify(userMapper).getUsers(eq("青"),
                    argThat(p -> p.getCurrent() == 2 && p.getSize() == 10));
            case "playlists" -> verify(playlistMapper).getPlaylists(eq("青"),
                    argThat(p -> p.getCurrent() == 2 && p.getSize() == 10));
            default -> throw new AssertionError("Unexpected resource: " + resource);
        }
    }

    @Test
    @DisplayName("未处理异常时，不应暴露内部错误详情")
    void hidesUnhandledExceptionDetails() {
        ResponseEntity<ResultModel<Void>> response =
                handler.handleException(new IllegalStateException("sensitive database detail"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody())
                .extracting(ResultModel::getCode, ResultModel::getMessage)
                .containsExactly(500, "服务器繁忙，请稍后再试");
    }
}
