package com.gumraze.drive.drive_backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gumraze.drive.drive_backend.auth.security.JwtAuthenticationFilter;
import com.gumraze.drive.drive_backend.auth.token.JwtAccessTokenValidator;
import com.gumraze.drive.drive_backend.common.api.ApiResponse;
import com.gumraze.drive.drive_backend.common.api.ResultCode;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final ObjectMapper objectMapper;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            JwtAccessTokenValidator jwtAccessTokenValidator
    ) {
        return new JwtAuthenticationFilter(jwtAccessTokenValidator);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) throws Exception {

        http
                // csrf 비활성화
                .csrf(csrf -> csrf.disable())

                // Stateless (세션 사용 안함)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 예외 처리
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(
                                (request, response, authException) ->
                                        writeErrorResponse(response, ResultCode.UNAUTHORIZED)
                        )
                        .accessDeniedHandler(
                                (request, response, accessDeniedException) ->
                                        writeErrorResponse(response, ResultCode.FORBIDDEN)
                        )
                )

                // 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/auth/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/",
                                "/actuator/health"
                        ).permitAll()

                        .requestMatchers("/users/me").hasRole("USER")

                        // GET 요청 처리
                        .requestMatchers(
                                HttpMethod.GET,
                                "/regions/**"
                        ).permitAll()
                        // POST 요청 처리
                        .requestMatchers(
                                HttpMethod.POST,
                                "users/profile/**"
                        ).permitAll()

                        .anyRequest().authenticated()
                )

                // JWT 필터 등록
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    private void writeErrorResponse(
            HttpServletResponse response,
            ResultCode code
    ) throws IOException {
        response.setStatus(code.httpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getWriter(), ApiResponse.failure(code));
    }
}
