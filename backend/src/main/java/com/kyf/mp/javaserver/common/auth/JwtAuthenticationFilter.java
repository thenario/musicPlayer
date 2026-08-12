package com.kyf.mp.javaserver.common.auth;

import com.kyf.mp.javaserver.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    public static final String USER_ID_ATTRIBUTE = "userId";
    public static final String TOKEN_ATTRIBUTE = "com.kyf.mp.javaserver.common.auth.JwtAuthenticationFilter.token";

    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = bearerToken(request);
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            authenticate(request, token);
        }
        filterChain.doFilter(request, response);
    }

    private void authenticate(HttpServletRequest request, String token) {
        try {
            Claims claims = JwtUtils.parseToken(token);
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
            log.debug("JWT authentication rejected: {}", e.getMessage());
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