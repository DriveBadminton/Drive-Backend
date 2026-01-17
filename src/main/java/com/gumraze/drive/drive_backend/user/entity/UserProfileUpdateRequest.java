package com.gumraze.drive.drive_backend.user.entity;

import com.gumraze.drive.drive_backend.user.constants.Gender;
import com.gumraze.drive.drive_backend.user.constants.Grade;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UserProfileUpdateRequest {
    private Grade regionalGrade;
    private Grade nationalGrade;
    private String birth;
    private Boolean birthVisible;
    private Long districtId;
    private String profileImageUrl;
    private Gender gender;
}
