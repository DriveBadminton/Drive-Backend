package com.gumraze.drive.drive_backend.user.service;

import com.gumraze.drive.drive_backend.user.dto.UserProfileCreateRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class UserProfileValidator {

    // 프로필 생성 시 검증 메서드
    public void validateForCreate(UserProfileCreateRequest request) {
        // nickname은 fallback(제3자 nickname)이 가능하므로 여기서 검증하지 않음.

        if (request.birth() == null || request.birth().isBlank()) {
            throw new IllegalArgumentException("Birth가 필요합니다");
        }

        if (request.gender() == null) {
            throw new IllegalArgumentException("gender가 필요합니다.");
        }
    }

    public void validateForUpdate(UserProfileCreateRequest request) {
        throw new UnsupportedOperationException();
    }

    public LocalDateTime parseBirthStartOfDay(String birth) {
        throw new UnsupportedOperationException();
    }
}
