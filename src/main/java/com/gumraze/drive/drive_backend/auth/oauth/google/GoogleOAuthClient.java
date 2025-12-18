package com.gumraze.drive.drive_backend.auth.oauth.google;

import com.gumraze.drive.drive_backend.auth.constants.AuthProvider;
import com.gumraze.drive.drive_backend.auth.oauth.OAuthClient;
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
    public String getProviderUserId(String authorizationCode, String redirectUri) {
        // Authorization Code -> Google Access Token
        GoogleTokenResponse tokenResponse = requestAccessToken(authorizationCode, redirectUri);

        // Google Access Token -> Google User Info
        GoogleUserResponse user = requestUserInfo(tokenResponse.accessToken());

        // providerUserId로 sub 변환
        return user.sub();
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
