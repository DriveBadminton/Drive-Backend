package com.gumraze.drive.drive_backend.user.dto;

import com.gumraze.drive.drive_backend.user.constants.Grade;

public record UserProfileCreateRequest(
        String nickname,
        Long regionId,
        Grade grade
) {
}
