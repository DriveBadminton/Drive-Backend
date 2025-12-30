package com.gumraze.drive.drive_backend.auth.oauth;

public interface OAuthClient {
    /**
 * Obtain OAuth user information using an authorization code and the original redirect URI.
 *
 * @param authorizationCode the authorization code received from the OAuth provider
 * @param redirectUri the redirect URI registered with the OAuth provider that was used in the authorization request
 * @return an OAuthUserInfo representing the authenticated user's information from the OAuth provider
 */
OAuthUserInfo getOAuthUserInfo(String authorizationCode, String redirectUri);
}