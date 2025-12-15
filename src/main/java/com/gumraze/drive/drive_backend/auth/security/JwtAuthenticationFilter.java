package com.gumraze.drive.drive_backend.auth.security;

import com.gumraze.drive.drive_backend.auth.token.JwtAccessTokenValidator;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

// OncePerRequestFilter -> 요청당 한 번만 실행되는 Spring Security Filter의 베이스 클래스
// OncePerRequestFilter로 하나의 HTTP 요청당 한 번만 doFilterInternal()이 실행됨.
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    // 필터는 Validator에 의존하도록 설정
    private final JwtAccessTokenValidator jwtAccessTokenValidator;

    public JwtAuthenticationFilter(JwtAccessTokenValidator jwtAccessTokenValidator) {
        this.jwtAccessTokenValidator = jwtAccessTokenValidator;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // authorization 헤더 확인
        String token = resolveToken(request);
        // 토큰 추출

        if (token != null) {
            // JWT 검증
            jwtAccessTokenValidator
                    .validateAndGetUserId(token)
                    .ifPresent(userId -> {
                        // Authentication 생성
                        var authentication =
                                new UsernamePasswordAuthenticationToken(
                                        userId,             // principal
                                        null,               // credentials
                                        List.of(() -> "ROLE_USER")  // authorities -> 아직 없음
                                );
                        // SecurityContext에 저장
                        SecurityContextHolder.getContext()
                                .setAuthentication(authentication);
                    });
        }
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        // Authorization 헤더 확인
        String header = request.getHeader(AUTHORIZATION_HEADER);

        // 헤더가 존재하면 Token을 확인
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(BEARER_PREFIX.length());
        }

        // 헤더가 없으면 토큰 없는 요청으로 그냥 통과 시킴.
        return null;
    }
}
