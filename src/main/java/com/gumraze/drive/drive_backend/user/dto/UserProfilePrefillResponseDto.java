package com.gumraze.drive.drive_backend.user.dto;

public record UserProfilePrefillResponseDto(
        String suggestedNickname,
        boolean hasOauthNickname
) { }
