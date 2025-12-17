package com.gumraze.drive.drive_backend.auth.oauth;

import com.gumraze.drive.drive_backend.auth.constants.AuthProvider;
import org.springframework.stereotype.Component;

@Component
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
