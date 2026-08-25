package com.kyf.mp.server.common.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import com.kyf.mp.server.common.BusinessException;

@ExtendWith(MockitoExtension.class)
class LoginRateLimiterTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private LoginRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        rateLimiter = new LoginRateLimiter(redisTemplate);
        ReflectionTestUtils.setField(rateLimiter, "maxAttempts", 5);
        ReflectionTestUtils.setField(rateLimiter, "windowSeconds", 900L);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("首次登录尝试时，应设置限流计数的过期时间")
    void setsExpiryForFirstAttempt() {
        when(valueOperations.increment(org.mockito.ArgumentMatchers.anyString())).thenReturn(1L);

        rateLimiter.check("alice");

        verify(redisTemplate).expire(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.eq(900L),
                org.mockito.ArgumentMatchers.eq(TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("登录尝试次数超过上限时，应拒绝请求")
    void rejectsAttemptsAboveConfiguredLimit() {
        when(valueOperations.increment(org.mockito.ArgumentMatchers.anyString())).thenReturn(6L);

        assertThatThrownBy(() -> rateLimiter.check("alice"))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getCode()).isEqualTo(429));
    }

    @Test
    @DisplayName("Redis 不可用时，应降级放行登录请求")
    void allowsLoginWhenRedisIsUnavailable() {
        when(valueOperations.increment(org.mockito.ArgumentMatchers.anyString()))
                .thenThrow(new RedisConnectionFailureException("redis unavailable"));

        rateLimiter.check("alice");
    }
}
