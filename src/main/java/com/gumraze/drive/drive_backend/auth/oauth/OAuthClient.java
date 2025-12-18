package com.gumraze.drive.drive_backend.auth.oauth;

public interface OAuthClient {
    String getProviderUserId(String authorizationCode, String redirectUri);
}
