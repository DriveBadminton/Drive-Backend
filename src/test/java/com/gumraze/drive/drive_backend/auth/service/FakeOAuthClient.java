package com.gumraze.drive.drive_backend.auth.service;

import com.gumraze.drive.drive_backend.auth.oauth.OAuthClient;
import com.gumraze.drive.drive_backend.auth.oauth.OAuthUserInfo;

public class FakeOAuthClient implements OAuthClient {

    private final OAuthUserInfo userInfo;

    // called는 fakeOAuthClient가 OAuthClient의 getOAuthUserInfo 메서드를 호출했는지 여부를 나타냄.
    private boolean called = false;


    public FakeOAuthClient(OAuthUserInfo userInfo) {
        this.userInfo = userInfo;
    }

    @Override
    public OAuthUserInfo getOAuthUserInfo(String authorizationCode, String redirectUri) {
        this.called = true;         // 테스트 검증용
        return userInfo;
    }

    public boolean isCalled() {
        return called;
    }
}
