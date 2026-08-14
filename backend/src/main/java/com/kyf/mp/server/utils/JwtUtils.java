package com.kyf.mp.server.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtils {
    private final SecretKey signingKey;
    private final long expireMillis;

    public JwtUtils(@Value("${jwt.secret}") String secret,
            @Value("${jwt.expire}") long expireMillis) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT_SECRET must contain at least 32 bytes");
        }
        if (expireMillis <= 0) {
            throw new IllegalStateException("jwt.expire must be positive");
        }
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expireMillis = expireMillis;
    }

    public String createToken(Long userId, String username) {
        return Jwts.builder().header().type("JWT").and().subject("music-user")
                .claim("user_id", userId).claim("user_name", username)
                .expiration(new Date(System.currentTimeMillis() + expireMillis)).signWith(signingKey).compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
    }
}