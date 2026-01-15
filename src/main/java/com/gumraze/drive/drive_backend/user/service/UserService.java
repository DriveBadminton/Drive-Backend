package com.gumraze.drive.drive_backend.user.service;

import com.gumraze.drive.drive_backend.user.dto.UserMeResponse;
import com.gumraze.drive.drive_backend.user.entity.User;

import java.util.Optional;

public interface UserService {
    Optional<User> findById(Long id);

    UserMeResponse getUserMe(Long userId);
}
