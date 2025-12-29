package com.gumraze.drive.drive_backend.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
public class ApiLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        // 로그 발생 시간 기록
        long start = System.currentTimeMillis();

        try {
            filterChain.doFilter(request, response);
        } finally {
            // 실행 시간 기록
            long duration = System.currentTimeMillis() - start;

            String method = request.getMethod();
            String path = request.getRequestURI();

            int status = response.getStatus();
            String userId = resolveUserId();

            log.info("[INFO][API] method={}, path={}, status={}, userId={}, duration={}",
                    method, path, status, userId, duration);
        }
    }

    private String resolveUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return "알 수 없는 사용자";
        }
        Object principal = auth.getPrincipal();

        return (principal instanceof Long) ? principal.toString() : "알 수 없는 사용자";
    }

    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request
    ) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator") || path.startsWith("/swagger");
    }
}
