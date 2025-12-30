package com.gumraze.drive.drive_backend.auth.oauth.kakao;

import com.gumraze.drive.drive_backend.auth.constants.AuthProvider;
import com.gumraze.drive.drive_backend.auth.oauth.OAuthClient;
import com.gumraze.drive.drive_backend.auth.oauth.OAuthUserInfo;
import com.gumraze.drive.drive_backend.auth.oauth.ProviderAwareOAuthClient;
import com.gumraze.drive.drive_backend.auth.oauth.kakao.dto.KakaoTokenResponse;
import com.gumraze.drive.drive_backend.auth.oauth.kakao.dto.KakaoUserResponse;
import com.gumraze.drive.drive_backend.user.constants.Gender;
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

    /**
     * Create a KaKaoOAuthClient configured with the provided HTTP client builder and Kakao OAuth properties.
     *
     * @param restClient a RestClient.Builder used to construct the RestClient for outgoing HTTP requests
     * @param properties KakaoOAuthProperties containing Kakao endpoint URIs and client credentials
     */
    public KaKaoOAuthClient(
            RestClient.Builder restClient,
            KakaoOAuthProperties properties
    ) {
        this.restClient = restClient.build();
        this.properties = properties;
    }

    /**
     * Obtain the authenticated Kakao user's information from an authorization code.
     *
     * Exchanges the provided authorization code for a Kakao access token and retrieves the user's profile, mapping available fields (id, email, nickname, profile images, gender, age range, birthday, and email verification) into an OAuthUserInfo.
     *
     * @param authorizationCode the authorization code received from Kakao after user authorization
     * @param redirectUri       the redirect URI used in the authorization request
     * @return an OAuthUserInfo populated with the user's id, email, nickname, profileImageUrl, thumbnailImageUrl, gender, ageRange, birthday, emailVerified, and phoneNumberVerified (set to `false` if not provided by Kakao)
     */
    @Override
    public OAuthUserInfo getOAuthUserInfo(String authorizationCode, String redirectUri) {
        try {
            // Authorization Code를 카카오 액세스 토큰으로 교환 -> Kakao Access Token
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

            KakaoUserResponse.KakaoAccount account =
                    userResponse == null
                            ? null
                            : userResponse.kakaoAccount();
            KakaoUserResponse.Profile profile =
                    account == null
                            ? null
                            : account.profile();

            // 응답 매핑
            String email = account == null ? null : account.email();
            String nickname = profile == null ? null : profile.nickname();
            String profileImageUrl = profile == null ? null : profile.profileImageUrl();
            String thumbnailImageUrl = profile == null ? null : profile.thumbnailImageUrl();
            Gender gender = null;
            if (account != null && account.gender() != null) {
                gender = Gender.valueOf(account.gender().toUpperCase());
            }
            boolean emailVerified =
                    account != null && Boolean.TRUE.equals(account.isEmailValid());

            return new OAuthUserInfo(
                    userResponse.id().toString(),
                    email,
                    nickname,
                    profileImageUrl,
                    thumbnailImageUrl,
                    gender,
                    account == null ? null : account.ageRange(),
                    account == null ? null : account.birthday(),
                    emailVerified,
                    false   // 카카오 응답에는 phoneNumberVerified 정보가 없음
            );
        } catch (RestClientResponseException e) {
            log.warn("[KAKAO][ERROR] status = {}, body = {}",
                  e.getStatusCode().value(),
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