package com.gumraze.drive.drive_backend.user.service;

import com.gumraze.drive.drive_backend.user.dto.UserProfileCreateRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class UserProfileValidator {

    // 프로필 생성 시 검증 메서드
    public void validateForCreate(UserProfileCreateRequest request) {
        if (request.nickname() == null || request.nickname().isBlank()) {
            throw new IllegalArgumentException("Nickname이 필요합니다.");
        }

        if (request.birth() == null || request.birth().isBlank()) {
            throw new IllegalArgumentException("Birth가 필요합니다.");
        }

        if (request.gender() == null) {
            throw new IllegalArgumentException("gender가 필요합니다.");
        }

        if (request.districtId() == null) {
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
