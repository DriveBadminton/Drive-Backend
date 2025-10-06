package com.gumraze.drive.drive_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import com.gumraze.drive.drive_backend.user.service.oauth.CustomOAuth2UserService;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/swagger-resources/**",
                    "/",
                    "/actuator/health",
                    "/oauth2/**",
                    "/login/**"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth -> oauth
                .userInfoEndpoint(info -> info.userService(customOAuth2UserService))
            )
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
