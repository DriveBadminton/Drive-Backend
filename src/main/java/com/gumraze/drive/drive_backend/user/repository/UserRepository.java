package com.gumraze.drive.drive_backend.user.repository;

import com.gumraze.drive.drive_backend.user.entity.User;

import java.util.Optional;

public interface UserRepository {

    Long createUser();

    Optional<User> findById(Long id);

    boolean existsById(Long userId);
}
