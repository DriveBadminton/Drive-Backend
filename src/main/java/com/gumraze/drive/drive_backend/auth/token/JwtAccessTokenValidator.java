package com.gumraze.drive.drive_backend.auth.token;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Optional;

@Component
public class JwtAccessTokenValidator {

    private final SecretKey secretKey;

    public JwtAccessTokenValidator(JwtProperties properties) {
        this.secretKey = Keys.hmacShaKeyFor(properties.accessToken().secret().getBytes());
    }

    public Optional<Long> validateAndGetUserId(String accessToken) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(accessToken)
                    .getBody();

            return Optional.of(Long.parseLong(claims.getSubject()));

        } catch (Exception e) {
            // 만료(exp), 서명 오류, 위변조, 형식 오류 등
            return Optional.empty();
        }
    }
}
