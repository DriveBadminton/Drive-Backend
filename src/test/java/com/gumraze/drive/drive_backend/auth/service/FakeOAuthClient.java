package com.gumraze.drive.drive_backend.auth.service;

import com.gumraze.drive.drive_backend.auth.oauth.OAuthClient;
import com.gumraze.drive.drive_backend.auth.oauth.OAuthUserInfo;

public class FakeOAuthClient implements OAuthClient {

    private final OAuthUserInfo userInfo;

    // called는 fakeOAuthClient가 OAuthClient의 getOAuthUserInfo 메서드를 호출했는지 여부를 나타냄.
    private boolean called = false;


    /**
     * Creates a fake OAuth client that will return the provided user information when invoked.
     *
     * @param userInfo the OAuth user information to be returned by this fake client
     */
    public FakeOAuthClient(OAuthUserInfo userInfo) {
        this.userInfo = userInfo;
    }

    /**
     * Provides the configured OAuth user info and marks the fake client as invoked for test verification.
     *
     * @param authorizationCode the OAuth authorization code (ignored by this fake)
     * @param redirectUri the OAuth redirect URI (ignored by this fake)
     * @return the configured {@link OAuthUserInfo} instance
     */
    @Override
    public OAuthUserInfo getOAuthUserInfo(String authorizationCode, String redirectUri) {
        this.called = true;         // 테스트 검증용
        return userInfo;
    }

    /**
     * Indicates whether {@code getOAuthUserInfo} has been invoked on this fake client.
     *
     * @return {@code true} if {@code getOAuthUserInfo} was called, {@code false} otherwise.
     */
    public boolean isCalled() {
        return called;
    }
}