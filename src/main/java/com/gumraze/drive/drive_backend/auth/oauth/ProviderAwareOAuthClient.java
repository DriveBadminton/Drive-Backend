package com.gumraze.drive.drive_backend.auth.oauth;

import com.gumraze.drive.drive_backend.auth.constants.AuthProvider;

public interface ProviderAwareOAuthClient {
    AuthProvider supports();
}
