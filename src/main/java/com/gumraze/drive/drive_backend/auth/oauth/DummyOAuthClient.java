package com.gumraze.drive.drive_backend.auth.oauth;

import com.gumraze.drive.drive_backend.auth.constants.AuthProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"local", "test"})
public class DummyOAuthClient implements OAuthClient, ProviderAwareOAuthClient {
    @Override
    public OAuthUserInfo getOAuthUserInfo(String authorizationCode, String redirectUri) {
        return new OAuthUserInfo(
                "dummy-oauth-user-id",
                null,
                "dummy-oauth-user-name",
                null,
                null,
                null,
                null,
                null,
                false,
                false
        );
    }

    @Override
    public AuthProvider supports() {
        return AuthProvider.DUMMY;
    }
}
