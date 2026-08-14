package com.kyf.mp.server.common.auth;

import com.kyf.mp.server.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TokenBlacklistService {

    private static final String KEY_PREFIX = "auth:revoked-token:";

    private final long defaultTtlMillis;
    private final JwtUtils jwtUtils;
    private final StringRedisTemplate redisTemplate;

    public TokenBlacklistService(JwtUtils jwtUtils, StringRedisTemplate redisTemplate,
            @Value("${jwt.expire}") long defaultTtlMillis) {
        this.jwtUtils = jwtUtils;
        this.redisTemplate = redisTemplate;
        this.defaultTtlMillis = defaultTtlMillis;
    }

    /** Revokes a token until its expiry, using a hash rather than the raw token as the Redis key. */
    public void revoke(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        long expireAt;
        try {
            Claims claims = jwtUtils.parseToken(token);
            Instant expiration = Optional.ofNullable(claims.getExpiration())
                    .map(date -> date.toInstant())
                    .orElse(null);
            expireAt = expiration != null ? expiration.toEpochMilli() : System.currentTimeMillis() + defaultTtlMillis;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("revoke: token already invalid, nothing to revoke: {}", e.getMessage());
            return;
        }

        long ttlMillis = Math.max(1L, expireAt - System.currentTimeMillis());
        redisTemplate.opsForValue().set(key(token), "1", Duration.ofMillis(ttlMillis));
    }

    /** Returns whether a token is revoked. Redis removes the marker automatically once its TTL expires. */
    public boolean isRevoked(String token) {
        return token != null && !token.isBlank() && Boolean.TRUE.equals(redisTemplate.hasKey(key(token)));
    }

    private static String key(String token) {
        return KEY_PREFIX + hash(token);
    }

    private static String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}