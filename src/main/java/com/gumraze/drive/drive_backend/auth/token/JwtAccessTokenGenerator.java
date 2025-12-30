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

    /**
     * Creates a JwtAccessTokenGenerator configured from the given JwtProperties.
     *
     * Initializes the HS256 signing key from properties.accessToken().secret() and sets the token
     * expiration duration from properties.accessToken().expirationMs().
     *
     * @param properties configuration holding the access token secret and expiration in milliseconds
     */
    public JwtAccessTokenGenerator(JwtProperties properties) {
        // HS256 알고리즘에 맞는 키를 생성함.
        secretKey = Keys.hmacShaKeyFor(properties.accessToken().secret().getBytes());
        expirationMs = properties.accessToken().expirationMs();
    }

    /**
     * Generate a JWT access token for the specified user.
     *
     * @param userId the user identifier to set as the token subject (`sub`)
     * @return the signed JWT access token as a compact string containing `sub`, `iat`, and `exp` claims
     */
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