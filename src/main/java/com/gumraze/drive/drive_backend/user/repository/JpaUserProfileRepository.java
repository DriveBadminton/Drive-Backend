package com.gumraze.drive.drive_backend.user.repository;

import com.gumraze.drive.drive_backend.user.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

// JPA Adapter
public interface JpaUserProfileRepository extends JpaRepository<UserProfile, Long> {
}

