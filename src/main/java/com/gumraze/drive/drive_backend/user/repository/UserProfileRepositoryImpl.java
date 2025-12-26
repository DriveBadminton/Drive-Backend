package com.gumraze.drive.drive_backend.user.repository;

import com.gumraze.drive.drive_backend.user.entity.UserProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// Port interface인 UserProfileRepository의 구현체
@Repository
@RequiredArgsConstructor
public class UserProfileRepositoryImpl implements UserProfileRepository {

    private final JpaUserProfileRepository jpaUserProfileRepository;

    @Override
    public boolean existsByUserId(Long userId) {
        return jpaUserProfileRepository.existsById(userId);
    }

    @Override
    public Optional<UserProfile> findByUserId(Long userId) {
        return jpaUserProfileRepository.findById(userId);
    }

    @Override
    public boolean existsById(Long userId) {
        return jpaUserProfileRepository.existsById(userId);
    }

    @Override
    public UserProfile save(UserProfile profile) {
        return jpaUserProfileRepository.save(profile);
    }
}
