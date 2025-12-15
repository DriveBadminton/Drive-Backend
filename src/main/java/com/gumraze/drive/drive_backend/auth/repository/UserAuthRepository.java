package com.gumraze.drive.drive_backend.auth.repository;

import com.gumraze.drive.drive_backend.auth.constants.AuthProvider;

import java.util.Optional;

public interface UserAuthRepository {

    Optional<Long> findUserId(
            AuthProvider provider,
            String providerUserId
    );

    void save(
            AuthProvider provider,
            String providerUserId,
            Long userId
    );
}
