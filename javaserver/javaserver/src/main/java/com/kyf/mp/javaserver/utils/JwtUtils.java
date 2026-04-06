package com.kyf.mp.javaserver.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtils {
    private static String SECRET;
    private static long EXPIRE;

    @Value("${jwt.secret}")
    public void setSecret(String secret) {
        JwtUtils.SECRET = secret;
    }

    @Value("${jwt.expire}")
    public void setExpire(long expire) {
        JwtUtils.EXPIRE = expire;
    }

    private JwtUtils() {
        // 工具类私有构造
    }

    public static String createToken(Integer userId, String username) {
        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setHeaderParam("alg", "HS256")
                .setSubject("music-user")
                .claim("user_id", userId)
                .claim("user_name", username)
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE))
                .signWith(SignatureAlgorithm.HS256, SECRET)
                .compact();
    }

    public static Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET)
                .parseClaimsJws(token)
                .getBody();
    }
}
