package com.gumraze.drive.drive_backend.auth.oauth;

public class FakeOAuthClient implements OAuthClient {

    // OAuthProvider가 반환한 사용자의 고유 식별자
    private final String providerUserId;
    // assertThat(fakeOAuthClient.isCalled()).isTrue();를 검증하기 위함.
    // 해당 OAuthClient가 호출되었는지 기록용.
    private boolean called = false;

    public FakeOAuthClient(String providerUserId) {
        this.providerUserId = providerUserId;
    }

    @Override
    public String getProviderUserId(String authorizationCode, String redirectUri) {
        this.called = true;         // 테스트 검증용
        return providerUserId;
    }

    public boolean isCalled() {
        return called;
    }
}
