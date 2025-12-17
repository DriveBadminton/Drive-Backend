package com.gumraze.drive.drive_backend.user.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

// Port interface인 UserProfileRepository의 구현체
@Repository
@RequiredArgsConstructor
public class UserProfileRepositoryImpl implements UserProfileRepository {

    private final JpaUserProfileRepository jpaUserProfileRepository;

    @Override
    public boolean existsByUserId(Long userId) {
        return jpaUserProfileRepository.existsById(userId);
    }
}
