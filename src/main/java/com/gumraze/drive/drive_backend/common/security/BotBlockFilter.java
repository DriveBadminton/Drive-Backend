package com.gumraze.drive.drive_backend.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
public class BotBlockFilter extends OncePerRequestFilter {

    private static final List<String> BLOCKED_PATH_PREFIXES = List.of(
            "/.git",
            "/dns-query",
            "/HNAP1",
            "/sdk",
            "/evox",
            "/odinhttpcall",
            "/autodiscover"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return BLOCKED_PATH_PREFIXES.stream().noneMatch(uri::startsWith);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        log.warn(
            "[BOT 차단] method={}, uri={}, ip={}",
            request.getMethod(),
            request.getRequestURI(),
            request.getRemoteAddr()
        );

        response.setStatus(HttpStatus.NOT_FOUND.value());
        return;
    }
}
