package com.kyf.mp.server.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kyf.mp.server.common.auth.JwtAuthenticationFilter;
import com.kyf.mp.server.common.auth.TokenBlacklistService;
import com.kyf.mp.server.utils.JwtUtils;
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
    @DisplayName("业务异常状态码非法时，应映射为 500")
    void mapsInvalidBusinessStatusToInternalServerError() {
        ResponseEntity<ResultModel<Void>> response =
                handler.handleBusinessException(new BusinessException(999, "无效状态"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getCode()).isEqualTo(500);
    }

    @Test
    @DisplayName("框架异常应保留 405、415、413、404，而不是统一变成 500")
    void frameworkErrorsKeepHttpSemantics() throws Exception {
        var mvc = MockMvcBuilders.standaloneSetup(new ErrorTestController())
                .setControllerAdvice(handler).build();

        mvc.perform(get("/errors/body"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().string(HttpHeaders.ALLOW, "POST"))
                .andExpect(jsonPath("$.code").value(405));
        mvc.perform(post("/errors/body").contentType(MediaType.TEXT_PLAIN).content("text"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.code").value(415));
        mvc.perform(post("/errors/body").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("请求体缺失或格式不正确"));
        mvc.perform(post("/errors/upload"))
                .andExpect(status().isPayloadTooLarge())
                .andExpect(jsonPath("$.code").value(413));
        mvc.perform(post("/errors/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("请求的资源不存在"));
    }

    @Test
    @DisplayName("Controller 内 Redis 连接失败应返回 503，且不暴露连接详情")
    void dependencyFailuresReturnServiceUnavailable() throws Exception {
        var mvc = MockMvcBuilders.standaloneSetup(new ErrorTestController())
                .setControllerAdvice(handler).build();

        mvc.perform(post("/errors/redis"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value(503))
                .andExpect(jsonPath("$.message").value("依赖服务暂不可用，请稍后重试"));
    }

    @Test
    @DisplayName("JWT 黑名单连接失败时应返回 503 JSON，并停止处理请求")
    void redisFailureInJwtFilterReturns503() throws Exception {
        var jwtUtils = new JwtUtils("test-secret-for-jwt-signing-must-be-at-least-32-bytes", 60_000L);
        var blacklist = mock(TokenBlacklistService.class);
        String token = jwtUtils.createToken(7L, "tester");
        when(blacklist.isRevoked(token)).thenThrow(new RedisConnectionFailureException("internal redis address"));
        var objectMapper = new ObjectMapper();
        var filter = new JwtAuthenticationFilter(blacklist, jwtUtils, objectMapper);
        var request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        var response = new MockHttpServletResponse();
        var chain = mock(jakarta.servlet.FilterChain.class);

        SecurityContextHolder.clearContext();
        try {
            filter.doFilter(request, response, chain);

            assertThat(response.getStatus()).isEqualTo(503);
            assertThat(response.getContentType()).startsWith(MediaType.APPLICATION_JSON_VALUE);
            var body = objectMapper.readTree(response.getContentAsString());
            assertThat(body.get("code").asInt()).isEqualTo(503);
            assertThat(body.get("message").asText()).isEqualTo("依赖服务暂不可用，请稍后重试");
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
            verify(chain, never()).doFilter(any(), any());
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @RestController
    static class ErrorTestController {
        @PostMapping(value = "/errors/body", consumes = MediaType.APPLICATION_JSON_VALUE)
        ResultModel<Void> body(@RequestBody java.util.Map<String, Object> body) {
            return ResultModel.success(null);
        }

        @PostMapping("/errors/upload")
        void upload() {
            throw new MaxUploadSizeExceededException(100);
        }

        @PostMapping("/errors/missing")
        void missing() {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "internal resource detail");
        }

        @PostMapping("/errors/redis")
        void redis() {
            throw new RedisConnectionFailureException("internal redis address");
        }
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
