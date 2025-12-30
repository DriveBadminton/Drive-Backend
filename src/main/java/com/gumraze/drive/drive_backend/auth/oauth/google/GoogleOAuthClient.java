package com.gumraze.drive.drive_backend.auth.oauth.google;

import com.gumraze.drive.drive_backend.auth.constants.AuthProvider;
import com.gumraze.drive.drive_backend.auth.oauth.OAuthClient;
import com.gumraze.drive.drive_backend.auth.oauth.OAuthUserInfo;
import com.gumraze.drive.drive_backend.auth.oauth.ProviderAwareOAuthClient;
import com.gumraze.drive.drive_backend.auth.oauth.google.dto.GoogleTokenResponse;
import com.gumraze.drive.drive_backend.auth.oauth.google.dto.GoogleUserResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Component
public class GoogleOAuthClient implements OAuthClient, ProviderAwareOAuthClient {

    private final GoogleOAuthProperties properties;
    private final RestClient restClient;

    public GoogleOAuthClient(
            GoogleOAuthProperties properties,
            RestClient.Builder restClient) {
        this.properties = properties;
        this.restClient = restClient.build();
    }


    @Override
    public OAuthUserInfo getOAuthUserInfo(String authorizationCode, String redirectUri) {
        // Authorization Code를 구글의 액세스 토큰으로 교환
        GoogleTokenResponse tokenResponse = requestAccessToken(authorizationCode, redirectUri);

        // 구글 액세스 토큰으로 구글에 저장된 사용자 정보 호출
        GoogleUserResponse user = requestUserInfo(tokenResponse.accessToken());

        // providerUserId로 sub 변환
        return new OAuthUserInfo(
                user.sub(),
                user.email(),
                user.name(),
                user.picture(),
                user.picture(),
                null,
                null,
                null,
                user.emailVerified() != null && user.emailVerified(),
                false
        );
    }

    private GoogleTokenResponse requestAccessToken(
            String authorizationCode,
            String redirectUri
    ) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("code", authorizationCode);
        form.add("redirect_uri", redirectUri);
        form.add("client_id", properties.clientId());
        form.add("client_secret", properties.clientSecret());

        return restClient.post()
                .uri(properties.tokenUri())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(GoogleTokenResponse.class);
    }

    private GoogleUserResponse requestUserInfo(String accessToken) {
        return restClient.get()
                .uri(properties.userInfoUri())
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(GoogleUserResponse.class);
    }


    @Override
    public AuthProvider supports() {
        return AuthProvider.GOOGLE;
    }
}
