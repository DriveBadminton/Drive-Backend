package com.gumraze.drive.drive_backend.user.service.oauth;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gumraze.drive.drive_backend.user.constants.OAuthProvider;
import com.gumraze.drive.drive_backend.user.entity.OauthUser;
import com.gumraze.drive.drive_backend.user.service.OauthUserService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
@Transactional
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final String REGISTRATION_ID_KAKAO = "kakao";

    private final OauthUserService oauthUserService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        if (!REGISTRATION_ID_KAKAO.equals(userRequest.getClientRegistration().getRegistrationId())) {
            return oAuth2User;
        }

        KakaoProfile profile = KakaoProfile.from(oAuth2User.getAttributes());

        if (profile.id() == null) {
            throw new OAuth2AuthenticationException(new OAuth2Error("invalid_kakao_response"),
                "Kakao response does not include an id attribute");
        }

        OauthUser user = oauthUserService.saveOrUpdate(
            OAuthProvider.KAKAO,
            profile.id(),
            profile.email(),
            profile.nickname(),
            profile.profileImageUrl()
        );

        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
        attributes.put("userId", Optional.ofNullable(user.getId()).orElse(-1L));

        Set<GrantedAuthority> authorities = Set.of(new SimpleGrantedAuthority("ROLE_USER"));

        return new DefaultOAuth2User(authorities, attributes, userRequest.getClientRegistration().getProviderDetails()
            .getUserInfoEndpoint().getUserNameAttributeName());
    }

    private record KakaoProfile(String id, String email, String nickname, String profileImageUrl) {

        @SuppressWarnings("unchecked")
        static KakaoProfile from(Map<String, Object> attributes) {
            Object id = attributes.get("id");
            Map<String, Object> account = (Map<String, Object>) attributes.getOrDefault("kakao_account", Map.of());
            Map<String, Object> profile = (Map<String, Object>) account.getOrDefault("profile", Map.of());

            String oauthId = id != null ? id.toString() : null;
            String email = account.getOrDefault("email", null) instanceof String emailValue ? emailValue : null;
            String nickname = profile.getOrDefault("nickname", null) instanceof String nicknameValue ? nicknameValue : null;
            String profileImageUrl = profile.getOrDefault("profile_image_url", null) instanceof String imageValue ? imageValue : null;

            return new KakaoProfile(oauthId, email, nickname, profileImageUrl);
        }
    }
}
