package com.kyf.mp.server.common.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kyf.mp.server.common.ResultModel;
import com.kyf.mp.server.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    public static final String USER_ID_ATTRIBUTE = "userId";
    public static final String TOKEN_ATTRIBUTE = "com.kyf.mp.server.common.auth.JwtAuthenticationFilter.token";

    private final TokenBlacklistService tokenBlacklistService;
    private final JwtUtils jwtUtils;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = bearerToken(request);
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                authenticate(request, token);
            } catch (DataAccessResourceFailureException | QueryTimeoutException e) {
                // 过滤器异常不会自动进入 ControllerAdvice；这里明确返回 503，且不能绕过黑名单。
                SecurityContextHolder.clearContext();
                log.error("JWT 黑名单查询不可用", e);
                response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                objectMapper.writeValue(response.getWriter(), ResultModel.error(
                        "依赖服务暂不可用，请稍后重试", HttpServletResponse.SC_SERVICE_UNAVAILABLE));
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private void authenticate(HttpServletRequest request, String token) {
        try {
            Claims claims = jwtUtils.parseToken(token);
            if (tokenBlacklistService.isRevoked(token)) {
                SecurityContextHolder.clearContext();
                log.debug("JWT token has been revoked, authentication rejected");
                return;
            }
            Object rawUserId = claims.get("user_id");
            if (!(rawUserId instanceof Number userId)) {
                return;
            }
            Long id = userId.longValue();
            UsernamePasswordAuthenticationToken authentication =
                    UsernamePasswordAuthenticationToken.authenticated(id, null, List.of());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            request.setAttribute(USER_ID_ATTRIBUTE, id);
            request.setAttribute(TOKEN_ATTRIBUTE, token);
        } catch (JwtException | IllegalArgumentException e) {
            SecurityContextHolder.clearContext();
            log.debug("JWT authentication rejected", e);
        }
    }

    private String bearerToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        String token = authorization.substring(7).trim();
        return token.isEmpty() ? null : token;
    }
}
