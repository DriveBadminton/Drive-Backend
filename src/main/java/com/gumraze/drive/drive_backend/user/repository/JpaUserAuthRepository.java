package com.gumraze.drive.drive_backend.user.repository;

import com.gumraze.drive.drive_backend.auth.constants.AuthProvider;
import com.gumraze.drive.drive_backend.user.entity.UserAuth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaUserAuthRepository extends JpaRepository<UserAuth, Long> {

    Optional<UserAuth> findByProviderAndProviderUserId(
            AuthProvider provider,
            String providerUserId
    );
}
