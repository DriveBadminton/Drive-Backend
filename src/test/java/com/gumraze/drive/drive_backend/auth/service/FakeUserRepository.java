package com.gumraze.drive.drive_backend.auth.service;

import com.gumraze.drive.drive_backend.user.constants.UserRole;
import com.gumraze.drive.drive_backend.user.constants.UserStatus;
import com.gumraze.drive.drive_backend.user.entity.User;
import com.gumraze.drive.drive_backend.user.repository.UserRepository;

import java.util.Optional;

public class FakeUserRepository implements UserRepository {
    private boolean createCalled = false;
    private long sequence = 100L;

    @Override
    public Long createUser() {
        createCalled = true;
        return sequence++;
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.of(
                new User(
                        UserStatus.ACTIVE,
                        UserRole.USER
                )
        );
    }

    @Override
    public boolean existsById(Long userId) {
        return true;
    }

    public boolean isCreateCalled() {
        return createCalled;
    }
}
