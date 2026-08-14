package com.kyf.mp.server.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class JwtUtilsTest {
    private static final String SECRET = "test-secret-for-jwt-signing-must-be-at-least-32-bytes";

    @Test
    void createsAndParsesToken() {
        JwtUtils jwtUtils = new JwtUtils(SECRET, 60_000L);

        var claims = jwtUtils.parseToken(jwtUtils.createToken(42L, "tester"));

        assertEquals(42L, claims.get("user_id", Number.class).longValue());
        assertEquals("tester", claims.get("user_name", String.class));
    }

    @Test
    void rejectsShortSecret() {
        assertThrows(IllegalStateException.class, () -> new JwtUtils("too-short", 60_000L));
    }

    @Test
    void rejectsNonPositiveExpiry() {
        assertThrows(IllegalStateException.class, () -> new JwtUtils(SECRET, 0L));
    }
}