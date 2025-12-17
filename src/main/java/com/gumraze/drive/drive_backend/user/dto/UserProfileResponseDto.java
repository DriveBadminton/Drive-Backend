package com.gumraze.drive.drive_backend.user.dto;

import com.gumraze.drive.drive_backend.user.constants.UserRole;
import com.gumraze.drive.drive_backend.user.constants.UserStatus;
import com.gumraze.drive.drive_backend.user.entity.User;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserProfileResponseDto {

    Long id;
    UserRole role;
    UserStatus status;

    public static UserProfileResponseDto from(
            User user
    ) {
        return UserProfileResponseDto.builder()
                .id(user.getId())
                .role(user.getRole())
                .status(user.getStatus())
                .build();
    }
}
