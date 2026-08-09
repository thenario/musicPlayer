package com.kyf.mp.javaserver.utils;

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
    private static String SECRET;
    private static long EXPIRE;

    @Value("${jwt.secret}")
    public void setSecret(String secret) { JwtUtils.SECRET = secret; }
    @Value("${jwt.expire}")
    public void setExpire(long expire) { JwtUtils.EXPIRE = expire; }
    private JwtUtils() { }

    public static String createToken(Integer userId, String username) {
        return Jwts.builder().header().type("JWT").and().subject("music-user")
                .claim("user_id", userId).claim("user_name", username)
                .expiration(new Date(System.currentTimeMillis() + EXPIRE)).signWith(signingKey()).compact();
    }

    public static Claims parseToken(String token) {
        return Jwts.parser().verifyWith(signingKey()).build().parseSignedClaims(token).getPayload();
    }

    private static SecretKey signingKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }
}