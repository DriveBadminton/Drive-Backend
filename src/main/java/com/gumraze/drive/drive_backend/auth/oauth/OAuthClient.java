package com.gumraze.drive.drive_backend.auth.oauth;

public interface OAuthClient {
    OAuthUserInfo getOAuthUserInfo(String authorizationCode, String redirectUri);
}
