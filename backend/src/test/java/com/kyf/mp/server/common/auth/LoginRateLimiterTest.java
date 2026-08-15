package com.kyf.mp.server.common.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
    void setsExpiryForFirstAttempt() {
        when(valueOperations.increment(org.mockito.ArgumentMatchers.anyString())).thenReturn(1L);

        rateLimiter.check("alice");

        verify(redisTemplate).expire(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.eq(900L),
                org.mockito.ArgumentMatchers.eq(TimeUnit.SECONDS));
    }

    @Test
    void rejectsAttemptsAboveConfiguredLimit() {
        when(valueOperations.increment(org.mockito.ArgumentMatchers.anyString())).thenReturn(6L);

        BusinessException exception = catchThrowableOfType(
                () -> rateLimiter.check("alice"), BusinessException.class);

        assertThat(exception.getCode()).isEqualTo(429);
    }

    @Test
    void allowsLoginWhenRedisIsUnavailable() {
        when(valueOperations.increment(org.mockito.ArgumentMatchers.anyString()))
                .thenThrow(new RedisConnectionFailureException("redis unavailable"));

        rateLimiter.check("alice");
    }
}