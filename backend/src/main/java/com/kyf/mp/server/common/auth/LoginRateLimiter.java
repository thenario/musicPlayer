package com.kyf.mp.server.common.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.kyf.mp.server.common.BusinessException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginRateLimiter {
    private static final String KEY_PREFIX = "auth:login-attempts:";

    private final StringRedisTemplate redisTemplate;

    @Value("${auth.login.max-attempts:5}")
    private int maxAttempts;

    @Value("${auth.login.window-seconds:900}")
    private long windowSeconds;

    public void check(String username) {
        Long attempts;
        try {
            String key = KEY_PREFIX + hash(username);
            attempts = redisTemplate.opsForValue().increment(key);
            if (attempts != null && attempts == 1L) {
                redisTemplate.expire(key, windowSeconds, TimeUnit.SECONDS);
            }
        } catch (RedisConnectionFailureException | RedisSystemException exception) {
            log.warn("Login rate limiting unavailable; allowing request: {}", exception.getMessage());
            return;
        }
        if (attempts != null && attempts > maxAttempts) {
            throw new BusinessException(429, "登录尝试过于频繁，请稍后再试");
        }
    }

    private static String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.trim().toLowerCase().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
    }
}