package com.gumraze.drive.drive_backend.user.service;

import com.gumraze.drive.drive_backend.user.dto.UserProfileCreateRequest;
import com.gumraze.drive.drive_backend.user.dto.UserProfilePrefillResponseDto;

public interface UserProfileService {

    // 프로필 신규 생성
    void createProfile(Long userId, UserProfileCreateRequest request);

    // 프로필 업데이트
    void updateProfile(Long userId, UserProfileCreateRequest request);

    // 제3자 로그인 계정의 닉네임 요청
    UserProfilePrefillResponseDto getProfilePrefill(Long userId);

}
