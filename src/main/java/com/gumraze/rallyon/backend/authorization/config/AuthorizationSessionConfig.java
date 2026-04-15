package com.gumraze.rallyon.backend.authorization.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.config.SessionRepositoryCustomizer;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.session.data.redis.config.ConfigureRedisAction;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisIndexedHttpSession;
import org.springframework.session.web.http.CookieHttpSessionIdResolver;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.session.web.http.HttpSessionIdResolver;

import java.time.Duration;

@Configuration
@ConditionalOnProperty(name = "spring.session.store-type", havingValue = "redis")
@EnableRedisIndexedHttpSession(redisNamespace = "${APP_AUTH_SESSION_NAMESPACE:rallyon:session}")
public class AuthorizationSessionConfig {

    @Bean
    public CookieSerializer cookieSerializer(AuthorizationProperties properties) {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        serializer.setCookieName("JSESSIONID");
        serializer.setCookiePath("/");
        serializer.setUseHttpOnlyCookie(true);
        serializer.setUseSecureCookie(properties.getCookies().isSecure());
        serializer.setSameSite(properties.getCookies().getSameSite());
        return serializer;
    }

    @Bean
    public HttpSessionIdResolver httpSessionIdResolver(CookieSerializer cookieSerializer) {
        CookieHttpSessionIdResolver resolver = new CookieHttpSessionIdResolver();
        resolver.setCookieSerializer(cookieSerializer);
        return resolver;
    }

    @Bean
    public ConfigureRedisAction configureRedisAction() {
        return ConfigureRedisAction.NO_OP;
    }

    @Bean
    public SessionRepositoryCustomizer<RedisIndexedSessionRepository> redisSessionRepositoryCustomizer(
            @Value("${APP_AUTH_BROWSER_SESSION_TIMEOUT:30m}") Duration sessionTimeout,
            @Value("${APP_AUTH_SESSION_NAMESPACE:rallyon:session}") String namespace
    ) {
        return sessionRepository -> {
            sessionRepository.setDefaultMaxInactiveInterval(sessionTimeout);
            sessionRepository.setRedisKeyNamespace(namespace);
        };
    }
}
