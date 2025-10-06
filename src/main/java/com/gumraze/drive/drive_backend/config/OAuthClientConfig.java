package com.gumraze.drive.drive_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

@Configuration
public class OAuthClientConfig {

    @Bean
    @ConditionalOnMissingBean(ClientRegistrationRepository.class)
    public ClientRegistrationRepository clientRegistrationRepository(
        @Value("${spring.security.oauth2.client.registration.kakao.client-id:dummy-kakao-client-id}") String clientId,
        @Value("${spring.security.oauth2.client.registration.kakao.client-secret:}") String clientSecret,
        @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri:{baseUrl}/login/oauth2/code/kakao}") String redirectUri,
        @Value("${spring.security.oauth2.client.provider.kakao.authorization-uri:https://kauth.kakao.com/oauth/authorize}") String authorizationUri,
        @Value("${spring.security.oauth2.client.provider.kakao.token-uri:https://kauth.kakao.com/oauth/token}") String tokenUri,
        @Value("${spring.security.oauth2.client.provider.kakao.user-info-uri:https://kapi.kakao.com/v2/user/me}") String userInfoUri,
        @Value("${spring.security.oauth2.client.provider.kakao.user-name-attribute:id}") String userNameAttribute
    ) {
        ClientRegistration.Builder builder = ClientRegistration.withRegistrationId("kakao")
            .clientId(clientId)
            .clientSecret(clientSecret)
            .redirectUri(redirectUri)
            .authorizationUri(authorizationUri)
            .tokenUri(tokenUri)
            .userInfoUri(userInfoUri)
            .userNameAttributeName(userNameAttribute)
            .clientAuthenticationMethod(org.springframework.security.oauth2.core.ClientAuthenticationMethod.CLIENT_SECRET_POST)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .scope("profile_nickname", "profile_image", "account_email")
            .clientName("Kakao");

        return new InMemoryClientRegistrationRepository(builder.build());
    }
}
