package com.gumraze.drive.drive_backend.auth.repository;

import java.util.Optional;

public interface UserAuthRepository {

    Optional<Long> findUserId(
            String provider,
            String providerUserId
    );

    void save(
            String provider,
            String providerUserId,
            Long userId
    );
}
