package com.kyf.mp.server.common.auth;

import com.kyf.mp.server.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TokenBlacklistService {

    private final ConcurrentHashMap<String, Long> revoked = new ConcurrentHashMap<>();
    private final long defaultTtlMillis;
    private final JwtUtils jwtUtils;

    public TokenBlacklistService(JwtUtils jwtUtils, @Value("${jwt.expire}") long defaultTtlMillis) {
        this.jwtUtils = jwtUtils;
        this.defaultTtlMillis = defaultTtlMillis;
    }

    // 吊销一个token,解析出exp后仅存其SHA-256哈希->过期时刻
    public void revoke(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        long expireAt;
        try {
            Claims claims = jwtUtils.parseToken(token);
            Instant exp = Optional.ofNullable(claims.getExpiration())
                    .map(d -> d.toInstant()) // 库返回的 Date 立刻转 Instant
                    .orElse(null);
            expireAt = (exp != null) ? exp.toEpochMilli() : System.currentTimeMillis() + defaultTtlMillis;
        } catch (JwtException | IllegalArgumentException e) {
            // 解析不了或已过期的token本就无法通过认证，无需入黑名单
            log.debug("revoke: token already invalid, nothing to revoke: {}", e.getMessage());
            return;
        }
        revoked.put(hash(token), expireAt);
    }

    // 该token是否处于吊销状态
    public boolean isRevoked(String token) {
        if (token == null) {
            return false;
        }
        String key = hash(token);
        Long expireAt = revoked.get(key);
        if (expireAt == null) {
            return false;
        }
        if (expireAt <= System.currentTimeMillis()) {
            revoked.remove(key, expireAt); // 防御性惰性清理，避免误删同 key 更新后的条目
            return false;
        }
        return true;
    }

    // 定期清理已过期条目，防止黑名单随登出次数无限增长
    @Scheduled(fixedDelay = 3_600_000)
    public void purgeExpired() {
        long now = System.currentTimeMillis();
        revoked.forEach((key, expireAt) -> {
            if (expireAt <= now) {
                revoked.computeIfPresent(key, (k, v) -> v <= now ? null : v);
            }
        });
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
