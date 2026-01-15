package com.gumraze.drive.drive_backend.user.service;

import com.gumraze.drive.drive_backend.user.dto.UserProfileCreateRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class UserProfileValidator {

    // 프로필 생성 시 검증 메서드
    public void validateForCreate(UserProfileCreateRequest request) {
        if (request.getNickname() == null || request.getNickname().isBlank()) {
            throw new IllegalArgumentException("Nickname이 필요합니다.");
        }

        if (request.getBirth() == null || request.getBirth().isBlank()) {
            throw new IllegalArgumentException("Birth가 필요합니다.");
        }

        if (request.getGender() == null) {
            throw new IllegalArgumentException("gender가 필요합니다.");
        }

        if (request.getDistrictId() == null) {
            throw new IllegalArgumentException("districtId가 필요합니다.");
        }
    }

    public void validateForUpdate(UserProfileCreateRequest request) {
        throw new UnsupportedOperationException();
    }

    public LocalDateTime parseBirthStartOfDay(String birth) {
        throw new UnsupportedOperationException();
    }
}
