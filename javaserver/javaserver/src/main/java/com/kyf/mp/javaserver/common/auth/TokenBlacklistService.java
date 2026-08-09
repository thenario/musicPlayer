package com.kyf.mp.javaserver.common.auth;

import com.kyf.mp.javaserver.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HexFormat;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * JWT 令牌黑名单（内存实现）。
 *
 * 登出时把 token 加入黑名单，后续请求携带该 token 时由 {@link JwtAuthenticationFilter} 判定失效。
 * key 存 token 的 SHA-256 哈希而非明文，value 存 token 的过期时刻，保证条目生命周期与 token 剩余有效期一致。
 * 方法签名（revoke / isRevoked）刻意保持简单，将来可平滑替换为 Redis 实现。
 */
@Component
@Slf4j
public class TokenBlacklistService {

    private final ConcurrentHashMap<String, Long> revoked = new ConcurrentHashMap<>();
    private final long defaultTtlMillis;

    public TokenBlacklistService(@Value("${jwt.expire}") long defaultTtlMillis) {
        this.defaultTtlMillis = defaultTtlMillis;
    }

    /** 吊销一个 token：解析出 exp 后仅存其 SHA-256 哈希 -> 过期时刻。 */
    public void revoke(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        long expireAt;
        try {
            Claims claims = JwtUtils.parseToken(token);
            Date exp = claims.getExpiration();
            expireAt = (exp != null) ? exp.getTime()
                    : System.currentTimeMillis() + defaultTtlMillis;
        } catch (JwtException | IllegalArgumentException e) {
            // 解析不了或已过期的 token 本就无法通过认证，无需入黑名单
            log.debug("revoke: token already invalid, nothing to revoke: {}", e.getMessage());
            return;
        }
        revoked.put(hash(token), expireAt);
    }

    /** 该 token 是否处于吊销状态。 */
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

    /** 定期清理已过期条目，防止黑名单随登出次数无限增长。 */
    @Scheduled(fixedDelay = 3_600_000)
    public void purgeExpired() {
        long now = System.currentTimeMillis();
        revoked.entrySet().removeIf(entry -> entry.getValue() <= now);
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
