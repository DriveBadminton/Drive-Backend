package com.gumraze.drive.drive_backend.auth.service;

import com.gumraze.drive.drive_backend.auth.constants.AuthProvider;
import com.gumraze.drive.drive_backend.auth.repository.UserAuthRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FakeUserAuthRepository implements UserAuthRepository {

    private final Map<String, Long> storage = new HashMap<>();

    @Override
    public Optional<Long> findUserId(
            AuthProvider provider,
            String providerUserId
    ) {
        return Optional.ofNullable(
                storage.get(provider + ":" + providerUserId)
        );
    }

    @Override
    public void save(
            AuthProvider provider,
            String providerUserId,
            Long userId
    ) {
        storage.put(provider + ":" + providerUserId, userId);
    }
}
