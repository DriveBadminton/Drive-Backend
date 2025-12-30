package com.gumraze.drive.drive_backend.user.service;

import com.gumraze.drive.drive_backend.user.constants.Gender;
import com.gumraze.drive.drive_backend.user.constants.Grade;
import com.gumraze.drive.drive_backend.user.dto.UserProfileCreateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserProfileValidatorTest {

    private UserProfileValidator validator;

    @BeforeEach
    void setUp() {
        validator = new UserProfileValidator();
    }

    @Test
    @DisplayName("프로필 생성 시 최종 닉네임이 null이면 예외가 발생함.")
    void validate_for_create_throws_when_resolved_nickname_is_null() {
        // given: 사용자 생성
        UserProfileCreateRequest request = new UserProfileCreateRequest(
                null,
                null,
                null,
                null,
                "19980925",
                Gender.MALE
        );

        // when & then
        assertThatThrownBy(() ->
                validator.validateForCreate(request)
        )
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("프로필 생성 시 최종 닉네임이 공백이면 예외가 발생함.")
    void validate_for_create_throes_when_resolved_nickname_is_blank() {
        // given
        UserProfileCreateRequest request = new UserProfileCreateRequest(
                "   ",
                null,
                null,
                null,
                "19980925",
                Gender.MALE
        );

        // when & then
        assertThatThrownBy(() ->
                validator.validateForCreate(request)
        )
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("프로필 생성 시 birth가 없으면 예외가 발생함")
    void validate_for_create_throws_when_birth_is_missing() {
        // given: 프로필 생성
        UserProfileCreateRequest request = new UserProfileCreateRequest(
                "Kim",
                null,
                null,
                null,
                null,
                Gender.MALE
        );

        // when: birth가 null이면,
        // then: 에러 발생
        assertThatThrownBy(() ->
                validator.validateForCreate(request)
        )
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("프로필 생성 시 gender가 없으면 예외가 발생함.")
    void validate_for_create_throws_when_gender_is_missing() {
        // given: 프로필 생성
        UserProfileCreateRequest request = new UserProfileCreateRequest(
                "Kim",
                null,
                null,
                null,
                "19980925",
                null
        );

        // when: gender가 null이면,
        // then: 에러 발생
        assertThatThrownBy(() ->
                validator.validateForCreate(request)
        )
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("일 수 없는 grade 문자열이면 예외가 발생한다.")
    void validate_for_create_throws_when_grade_is_invalid() {
        // given: Grade 검증
        assertThatThrownBy(() -> Grade.from("invalid-grade"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("birth의 값이 blank이면 예외가 발생한다.")
    void validate_for_create_throws_when_birth_is_blank() {
        // given & when: birth가 blank인 프로필 생성
        UserProfileCreateRequest request = new UserProfileCreateRequest(
                "Kim",
                null,
                null,
                null,
                "   ",
                Gender.MALE
        );

        // then: 예외 발생
        assertThatThrownBy(() -> validator.validateForCreate(request))
                .isInstanceOf(IllegalArgumentException.class);
    }
}