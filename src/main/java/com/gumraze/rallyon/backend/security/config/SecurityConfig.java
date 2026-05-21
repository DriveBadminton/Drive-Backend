package com.gumraze.rallyon.backend.security.config;

import com.gumraze.rallyon.backend.common.logging.ApiLoggingFilter;
import com.gumraze.rallyon.backend.common.security.BotBlockFilter;
import com.gumraze.rallyon.backend.security.resourceserver.CookieBearerTokenResolver;
import com.gumraze.rallyon.backend.security.resourceserver.JwtRoleAuthenticationConverter;
import com.gumraze.rallyon.backend.security.resourceserver.ResourceServerProperties;
import com.gumraze.rallyon.backend.security.web.HostRequestMatchers;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties({CorsProperties.class, ResourceServerProperties.class})
@RequiredArgsConstructor
public class SecurityConfig {

    private final ObjectMapper objectMapper;
    private final CorsProperties corsProperties;
    private final ResourceServerProperties resourceServerProperties;

    @Bean
    public ApiLoggingFilter apiLoggingFilter() {
        return new ApiLoggingFilter();
    }

    @Bean
    public BotBlockFilter botBlockFilter() {
        return new BotBlockFilter();
    }

    @Bean
    public BearerTokenResolver bearerTokenResolver() {
        return new CookieBearerTokenResolver(resourceServerProperties.getAccessTokenName());
    }

    @Bean
    @Order(3)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        RequestMatcher apiHostMatcher = HostRequestMatchers.forConfiguredHost(resourceServerProperties.getHost());

        http
                .securityMatcher(apiHostMatcher)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(
                                (request, response, authException) -> writeErrorResponse(
                                        request,
                                        response,
                                        HttpStatus.UNAUTHORIZED,
                                        "/problems/unauthorized",
                                        "인증되지 않은 사용자입니다.",
                                        "인증되지 않은 사용자입니다."
                                )
                        )
                        .accessDeniedHandler(
                                (request, response, accessDeniedException) -> writeErrorResponse(
                                        request,
                                        response,
                                        HttpStatus.FORBIDDEN,
                                        "/problems/forbidden",
                                        "접근 권한이 없습니다.",
                                        "접근 권한이 없습니다."
                                )
                        )
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/oauth2/**",
                                "/.well-known/**",
                                "/login",
                                "/identity/**"
                        ).denyAll()
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/",
                                "/error",
                                "/actuator/health"
                        ).permitAll()
                        .requestMatchers(
                                HttpMethod.GET,
                                "/regions/**",
                                "/free-games/share/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .bearerTokenResolver(bearerTokenResolver())
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(new JwtRoleAuthenticationConverter()))
                )
                .addFilterBefore(botBlockFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(apiLoggingFilter(), BotBlockFilter.class);

        return http.build();
    }

    @Bean
    @Order(99)
    public SecurityFilterChain denyUnroutedRequests(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().denyAll());

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        List<String> allowedOrigins = corsProperties.allowedOrigins();
        if (allowedOrigins.isEmpty()) {
            throw new IllegalStateException("app.cors.allowed-origins must be configured");
        }

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private void writeErrorResponse(
            HttpServletRequest request,
            HttpServletResponse response,
            HttpStatus status,
            String type,
            String title,
            String detail
    ) throws IOException {
        ProblemDetail problem = ProblemDetail.forStatus(status);
        problem.setType(URI.create(type));
        problem.setTitle(title);
        problem.setDetail(detail);
        if (request != null) {
            problem.setInstance(URI.create(request.getRequestURI()));
        }

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getWriter(), problem);
    }
}
