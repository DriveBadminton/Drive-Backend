package com.gumraze.drive.drive_backend.auth.service;

import com.gumraze.drive.drive_backend.auth.constants.AuthProvider;
import com.gumraze.drive.drive_backend.auth.oauth.OAuthClient;
import com.gumraze.drive.drive_backend.auth.oauth.OAuthClientResolver;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class FakeOAuthClientResolver extends OAuthClientResolver {

    private final Map<AuthProvider, OAuthClient> clients =
            new EnumMap<>(AuthProvider.class);

    public FakeOAuthClientResolver() {
        super(Collections.emptyList());
    }

    public void register(AuthProvider provider, OAuthClient client) {
        clients.put(provider, client);
    }

    @Override
    public OAuthClient resolve(AuthProvider provider) {
        OAuthClient client = clients.get(provider);
        if (client == null) {
            throw new IllegalArgumentException("지원되지 않는 OAuth 클라이언트: " + provider);
        }
        return client;
    }
}
