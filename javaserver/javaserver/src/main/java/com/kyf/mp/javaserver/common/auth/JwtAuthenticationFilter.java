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
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    public static final String USER_ID_ATTRIBUTE = "userId";
    public static final String TOKEN_ATTRIBUTE = JwtAuthenticationFilter.class.getName() + ".token";

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
            Object rawUserId = claims.get("user_id");
            if (!(rawUserId instanceof Number userId)) {
                return;
            }
            Integer id = userId.intValue();
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