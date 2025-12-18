package com.gumraze.drive.drive_backend.user.repository;

// Port 인터페이스로 사용
public interface UserProfileRepository {
    boolean existsByUserId(Long userId);
}

