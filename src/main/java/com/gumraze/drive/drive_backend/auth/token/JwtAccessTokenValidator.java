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

    /**
     * Create a JwtAccessTokenValidator and initialize the HMAC secret key used to verify access tokens.
     *
     * @param properties configuration containing the access token settings; the HMAC-SHA key is derived from properties.accessToken().secret()
     */
    public JwtAccessTokenValidator(JwtProperties properties) {
        this.secretKey = Keys.hmacShaKeyFor(properties.accessToken().secret().getBytes());
    }

    /**
     * Validate a JWT access token and extract the user ID from its subject.
     *
     * If the token is valid and its subject can be parsed as a `Long`, the returned
     * Optional contains that user ID; otherwise the returned Optional is empty.
     *
     * @param accessToken the JWT access token string to validate
     * @return the user ID parsed from the token's subject if validation succeeds, `Optional.empty()` otherwise
     */
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