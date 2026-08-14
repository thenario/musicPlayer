package com.kyf.mp.server.common.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.kyf.mp.server.utils.JwtUtils;
import org.junit.jupiter.api.Test;

class TokenBlacklistServiceTest {

    private static final String JWT_SECRET = "test-jwt-secret-key-must-contain-at-least-32-bytes";

    private final JwtUtils jwtUtils = new JwtUtils(JWT_SECRET, 60_000L);

    @Test
    void revokingValidTokenMarksItAsRevoked() {
        TokenBlacklistService service = new TokenBlacklistService(jwtUtils, 60_000L);
        String token = jwtUtils.createToken(1L, "tester");

        service.revoke(token);

        assertThat(service.isRevoked(token)).isTrue();
    }

    @Test
    void revokingInvalidTokenDoesNotAddItToBlacklist() {
        TokenBlacklistService service = new TokenBlacklistService(jwtUtils, 60_000L);
        String invalidToken = "not-a-jwt";

        service.revoke(invalidToken);

        assertThat(service.isRevoked(invalidToken)).isFalse();
    }
}
