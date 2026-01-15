package com.gumraze.drive.drive_backend.user.dto;

import com.gumraze.drive.drive_backend.user.constants.UserStatus;
import com.gumraze.drive.drive_backend.user.entity.User;
import com.gumraze.drive.drive_backend.user.entity.UserProfile;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserMeResponse {
    UserStatus status;
    String profileImageUrl;
    String nickname;

    public static UserMeResponse from(
            User user,
            UserProfile profile
    ) {
        return UserMeResponse.builder()
                .status(user.getStatus())
                .profileImageUrl(profile != null ? profile.getProfileImageUrl() : null)
                .nickname(profile != null ? profile.getNickname() : null)
                .build();
    }
}