package com.gumraze.drive.drive_backend.auth.oauth;

import com.gumraze.drive.drive_backend.auth.constants.AuthProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"local", "test"})
public class DummyOAuthClient implements OAuthClient, ProviderAwareOAuthClient {
    /**
     * Provides a fixed dummy OAuth user info for the supplied authorization code and redirect URI.
     *
     * <p>This implementation returns a prepopulated {@code OAuthUserInfo} with constant dummy values
     * and does not depend on the provided parameters.</p>
     *
     * @param authorizationCode the authorization code from the OAuth provider
     * @param redirectUri the redirect URI used in the OAuth flow
     * @return an {@code OAuthUserInfo} containing a constant dummy user id and display name; other
     *         fields are null and boolean flags are false
     */
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

    /**
     * Indicates which authentication provider this client supports.
     *
     * @return the supported AuthProvider, specifically {@link AuthProvider#DUMMY}
     */
    @Override
    public AuthProvider supports() {
        return AuthProvider.DUMMY;
    }
}