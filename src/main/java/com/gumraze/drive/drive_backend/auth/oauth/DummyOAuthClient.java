package com.gumraze.drive.drive_backend.auth.oauth;

import com.gumraze.drive.drive_backend.auth.constants.AuthProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"local", "test"})
public class DummyOAuthClient implements OAuthClient, ProviderAwareOAuthClient {
    @Override
    public String getProviderUserId(String authorizationCode, String redirectUri) {
        return "dummy-oauth-user-id";
    }

    @Override
    public AuthProvider supports() {
        return AuthProvider.DUMMY;
    }
}
