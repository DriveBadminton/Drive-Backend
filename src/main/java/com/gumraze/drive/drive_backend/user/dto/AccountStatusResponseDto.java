package com.gumraze.drive.drive_backend.user.dto;

import com.gumraze.drive.drive_backend.user.constants.UserStatus;

public record AccountStatusResponseDto(
        Long userId,
        UserStatus status,
        boolean hasProfile
) { }