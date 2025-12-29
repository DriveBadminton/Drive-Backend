package com.gumraze.drive.drive_backend.auth.oauth.kakao;

import com.gumraze.drive.drive_backend.auth.constants.AuthProvider;
import com.gumraze.drive.drive_backend.auth.oauth.OAuthClient;
import com.gumraze.drive.drive_backend.auth.oauth.ProviderAwareOAuthClient;
import com.gumraze.drive.drive_backend.auth.oauth.kakao.dto.KakaoTokenResponse;
import com.gumraze.drive.drive_backend.auth.oauth.kakao.dto.KakaoUserResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Component
public class KaKaoOAuthClient implements OAuthClient, ProviderAwareOAuthClient {

    private final RestClient restClient;
    private final KakaoOAuthProperties properties;

    public KaKaoOAuthClient(
            RestClient.Builder restClient,
            KakaoOAuthProperties properties
    ) {
        this.restClient = restClient.build();
        this.properties = properties;
    }

    @Override
    public String getProviderUserId(String authorizationCode, String redirectUri) {
        try {
            // Authorization Code -> Kakao Access Token
            KakaoTokenResponse tokenResponse = restClient.post()
                    .uri(properties.tokenUri())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(
                            "grant_type=authorization_code" +
                                    "&client_id=" + properties.clientId() +
                                    "&client_secret=" + properties.clientSecret() +
                                    "&redirect_uri=" + redirectUri +
                                    "&code=" + authorizationCode
                    )
                    .retrieve()
                    .body(KakaoTokenResponse.class);

            // 토큰 응답 로그
            log.info("[KAKAO][TOKEN] expiresIn = {}, scope = {}, tokenPrefix = {}",
                    tokenResponse.expiresIn(),
                    tokenResponse.scope(),
                    tokenResponse.tokenType()
            );

            // Kakao Access token -> Kakao User ID
            KakaoUserResponse userResponse = restClient.get()
                    .uri(properties.userInfoUri())
                    .header("Authorization", "Bearer " + tokenResponse.accessToken())
                    .retrieve()
                    .body(KakaoUserResponse.class);

            // 유저 응답 로그
            log.info("[KAKAO][USER][RAW] {}",
                    userResponse
            );

            return userResponse.id().toString();
        } catch (RestClientResponseException e) {
            log.warn("[KAKAO][ERROR] status = {}, body = {}",
                    e.getRawStatusCode(),
                    e.getResponseBodyAsString()
            );
            throw e;
        }
    }

    @Override
    public AuthProvider supports() {
        return AuthProvider.KAKAO;
    }
}
