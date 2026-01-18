package com.gumraze.drive.drive_backend.user.service;

import com.gumraze.drive.drive_backend.user.dto.UserProfileCreateRequest;
import com.gumraze.drive.drive_backend.user.dto.UserProfileIdentityUpdateRequest;
import com.gumraze.drive.drive_backend.user.dto.UserProfilePrefillResponseDto;
import com.gumraze.drive.drive_backend.user.dto.UserProfileResponseDto;
import com.gumraze.drive.drive_backend.user.entity.UserProfileUpdateRequest;

public interface UserProfileService {

    // 프로필 신규 생성
    void createProfile(Long userId, UserProfileCreateRequest request);

    // 프로필 업데이트
    void updateProfile(Long userId, UserProfileCreateRequest request);

    // 제3자 로그인 계정의 닉네임 요청
    UserProfilePrefillResponseDto getProfilePrefill(Long userId);

    // 프로필 조회
    UserProfileResponseDto getMyProfile(Long userId);

    // 프로필 수정
    void updateMyProfile(Long userId, UserProfileUpdateRequest request);

    // 닉네임 + 태그 수정
    void updateNicknameAndTags(Long userId, UserProfileIdentityUpdateRequest request);
}
