package com.gumraze.drive.drive_backend.auth.oauth;

import com.gumraze.drive.drive_backend.user.constants.Gender;

// provider에 관계없이 동일한 형태로 서비스 계층에 전달하기 위한 표준 OAuth 레코드
public record OAuthUserInfo(
        String providerUserId,
        String email,
        String nickname,
        String profileImageUrl,
        String thumbnailImageUrl,
        Gender gender,
        String ageRange,
        String birthday,
        boolean emailVerified,
        boolean phoneNumberVerified
) {
}
