package com.kyf.mp.server.common.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.kyf.mp.server.utils.JwtUtils;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class TokenBlacklistServiceTest {

    private static final String JWT_SECRET = "test-jwt-secret-key-must-contain-at-least-32-bytes";

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private final JwtUtils jwtUtils = new JwtUtils(JWT_SECRET, 60_000L);
    private TokenBlacklistService service;

    @BeforeEach
    void setUp() {
        service = new TokenBlacklistService(jwtUtils, redisTemplate, 60_000L);
    }

    @Test
    void revokingValidTokenStoresHashedKeyWithTtl() {
        String token = jwtUtils.createToken(1L, "tester");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        service.revoke(token);

        ArgumentCaptor<String> key = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Duration> ttl = ArgumentCaptor.forClass(Duration.class);
        verify(valueOperations).set(key.capture(), eq("1"), ttl.capture());
        assertThat(key.getValue()).startsWith("auth:revoked-token:");
        assertThat(ttl.getValue()).isPositive();
    }

    @Test
    void recognizesRedisBlacklistMarker() {
        String token = jwtUtils.createToken(1L, "tester");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        service.revoke(token);
        ArgumentCaptor<String> key = ArgumentCaptor.forClass(String.class);
        verify(valueOperations).set(key.capture(), eq("1"), any(Duration.class));
        when(redisTemplate.hasKey(key.getValue())).thenReturn(true);

        assertThat(service.isRevoked(token)).isTrue();
    }

    @Test
    void ignoresInvalidTokenDuringRevocation() {
        service.revoke("not-a-jwt");

        verifyNoInteractions(redisTemplate);
    }
}