package com.gumraze.drive.drive_backend.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class UserSearchResponse {
    Long userId;
    String nickname;
    String tag;
    String profileImageUrl;
}
