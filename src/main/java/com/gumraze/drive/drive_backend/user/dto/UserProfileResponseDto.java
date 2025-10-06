package com.gumraze.drive.drive_backend.user.dto;

import com.gumraze.drive.drive_backend.user.entity.OauthUser;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserProfileResponseDto {
    Long id;
    String email;
    String nickname;
    String profileImageUrl;

    public static UserProfileResponseDto from(OauthUser user) {
        return UserProfileResponseDto.builder()
            .id(user.getId())
            .email(user.getEmail())
            .nickname(user.getNickname())
            .profileImageUrl(user.getProfileImageUrl())
            .build();
    }
}
