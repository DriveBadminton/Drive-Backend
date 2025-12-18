package com.gumraze.drive.drive_backend.auth.token;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtAccessTokenGenerator implements TokenProvider {

    // JWT를 서명할 때 사용하는 비밀키로 토큰이 위•변조되지 않았음을 증명함.
    private final SecretKey secretKey;
    private final long expirationMs;

    public JwtAccessTokenGenerator(JwtProperties properties) {
        // HS256 알고리즘에 맞는 키를 생성함.
        secretKey = Keys.hmacShaKeyFor(properties.secret().getBytes());
        expirationMs = properties.expirationMs();
    }

    @Override
    public String generateAccessToken(Long userId) {
        Instant now = Instant.now();

        return Jwts.builder()
                .setSubject(String.valueOf(userId))     // sub
                .setIssuedAt(Date.from(now))            // iat
                .setExpiration(Date.from(now.plusMillis(expirationMs)))  // exp
                .signWith(secretKey)
                .compact();
    }
}
