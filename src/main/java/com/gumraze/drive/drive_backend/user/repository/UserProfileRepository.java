package com.gumraze.drive.drive_backend.user.repository;

import com.gumraze.drive.drive_backend.user.entity.UserProfile;

import java.util.Optional;

// Port 인터페이스로 사용
public interface UserProfileRepository {
    boolean existsByUserId(Long userId);

    Optional<UserProfile> findByUserId(Long userId);
}

