package com.kyf.mp.server.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

class JwtUtilsTest {
    private static final String SECRET = "test-secret-for-jwt-signing-must-be-at-least-32-bytes";

    @Test
    @DisplayName("创建的 JWT 应能解析出用户信息")
    void createsAndParsesToken() {
        JwtUtils jwtUtils = new JwtUtils(SECRET, 60_000L);

        var claims = jwtUtils.parseToken(jwtUtils.createToken(42L, "tester"));

        assertEquals(42L, claims.get("user_id", Number.class).longValue());
        assertEquals("tester", claims.get("user_name", String.class));
    }

    @Test
    @DisplayName("过短的 JWT 密钥应被拒绝")
    void rejectsShortSecret() {
        assertThrows(IllegalStateException.class, () -> new JwtUtils("too-short", 60_000L));
    }

    @Test
    @DisplayName("非正数的 JWT 有效期应被拒绝")
    void rejectsNonPositiveExpiry() {
        assertThrows(IllegalStateException.class, () -> new JwtUtils(SECRET, 0L));
    }
}
