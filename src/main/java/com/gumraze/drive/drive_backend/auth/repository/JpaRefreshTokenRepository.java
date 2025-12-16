package com.gumraze.drive.drive_backend.auth.repository;

import com.gumraze.drive.drive_backend.auth.entity.RefreshToken;
import com.gumraze.drive.drive_backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaRefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByUser(User user);

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    void deleteByUser(User user);
}
