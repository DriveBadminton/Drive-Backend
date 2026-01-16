package com.gumraze.drive.drive_backend.user.dto;

import com.gumraze.drive.drive_backend.user.constants.Gender;
import com.gumraze.drive.drive_backend.user.constants.Grade;
import com.gumraze.drive.drive_backend.user.constants.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class UserProfileResponseDto {
    private UserStatus status;
    private String nickname;
    private String tag;
    private String profileImageUrl;
    private Gender gender;
    private LocalDateTime birth;
    private boolean birthVisible;
    private Grade regionalGrade;
    private Grade nationalGrade;
    private String districtName;
    private String provinceName;
    private LocalDateTime tagChangedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
