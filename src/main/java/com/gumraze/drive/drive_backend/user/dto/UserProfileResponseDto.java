package com.gumraze.drive.drive_backend.user.dto;

import com.gumraze.drive.drive_backend.user.constants.UserRole;
import com.gumraze.drive.drive_backend.user.constants.UserStatus;
import com.gumraze.drive.drive_backend.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserProfileResponseDto {

    @Schema(description = "사용자 ID", example = "1")
    Long id;
    @Schema(description = "사용자 역할", example = "ROLE_USER")
    UserRole role;
    @Schema(description = "계정 상태", example = "ACTIVE")
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
