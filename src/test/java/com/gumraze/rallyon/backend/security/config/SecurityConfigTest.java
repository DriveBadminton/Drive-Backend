package com.gumraze.rallyon.backend.security.config;

import com.gumraze.rallyon.backend.security.resourceserver.ResourceServerProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigTest {

    @Test
    @DisplayName("CORS preflight에서 PATCH 메서드를 허용한다")
    void corsConfigurationAllowsPatchMethod() {
        SecurityConfig securityConfig = new SecurityConfig(
                new ObjectMapper(),
                new CorsProperties(List.of("https://rallyon.test")),
                new ResourceServerProperties()
        );

        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/free-games/1");
        request.addHeader("Origin", "https://rallyon.test");
        request.addHeader("Access-Control-Request-Method", "PATCH");

        var configuration = securityConfig.corsConfigurationSource().getCorsConfiguration(request);

        assertThat(configuration).isNotNull();
        assertThat(configuration.getAllowedMethods()).contains("PATCH");
    }
}
