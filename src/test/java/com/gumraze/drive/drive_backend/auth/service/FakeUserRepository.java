package com.gumraze.drive.drive_backend.auth.service;

import com.gumraze.drive.drive_backend.user.repository.UserRepository;

public class FakeUserRepository implements UserRepository {
    private boolean createCalled = false;
    private long sequence = 100L;

    @Override
    public Long createUser() {
        createCalled = true;
        return sequence++;
    }

    public boolean isCreateCalled() {
        return createCalled;
    }
}
