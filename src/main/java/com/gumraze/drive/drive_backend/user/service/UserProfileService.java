package com.gumraze.drive.drive_backend.user.service;

import com.gumraze.drive.drive_backend.user.dto.UserProfileCreateRequest;

public interface UserProfileService {

    // 프로필 신규 생성
    void createProfile(Long userId, UserProfileCreateRequest request);

    // 프로필 업데이트
    void updateProfile(Long userId, UserProfileCreateRequest request);

}
