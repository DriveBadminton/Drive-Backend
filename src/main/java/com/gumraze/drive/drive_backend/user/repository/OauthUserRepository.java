package com.gumraze.drive.drive_backend.user.repository;

import java.util.Optional;

import com.gumraze.drive.drive_backend.user.constants.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import com.gumraze.drive.drive_backend.user.entity.OauthUser;

public interface OauthUserRepository extends JpaRepository<OauthUser, Long> {

    Optional<OauthUser> findByOauthProviderAndOauthId(AuthProvider oauthProvider, String oauthId);
}
