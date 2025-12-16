package com.gumraze.drive.drive_backend.auth.oauth.kakao;

import com.gumraze.drive.drive_backend.auth.constants.AuthProvider;
import com.gumraze.drive.drive_backend.auth.oauth.OAuthClient;
import com.gumraze.drive.drive_backend.auth.oauth.ProviderAwareOAuthClient;
import com.gumraze.drive.drive_backend.auth.oauth.kakao.dto.KakaoTokenResponse;
import com.gumraze.drive.drive_backend.auth.oauth.kakao.dto.KakaoUserResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

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


        // Kakao Access token -> Kakao User ID
        KakaoUserResponse userResponse = restClient.get()
                .uri(properties.userInfoUri())
                .header("Authorization", "Bearer " + tokenResponse)
                .retrieve()
                .body(KakaoUserResponse.class);

        return userResponse.id().toString();
    }

    @Override
    public AuthProvider supports() {
        return AuthProvider.KAKAO;
    }
}
