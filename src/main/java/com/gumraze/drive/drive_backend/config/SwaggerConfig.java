package com.gumraze.drive.drive_backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI driveBackendOpenAPI() {
        return new OpenAPI()
            .info(apiInfo())
            .components(new Components().addSecuritySchemes(SECURITY_SCHEME_NAME, bearerAuthScheme()))
            .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }

    private Info apiInfo() {
        return new Info()
            .title("Drive Backend API")
            .description("동호인 배드민턴 대회/선수 관리 백엔드 API 명세")
            .version("0.0.1");
    }

    private SecurityScheme bearerAuthScheme() {
        return new SecurityScheme()
            .name(SECURITY_SCHEME_NAME)
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT");
    }
}
