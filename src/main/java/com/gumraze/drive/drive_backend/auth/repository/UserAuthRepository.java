package com.gumraze.drive.drive_backend.auth.repository;

import com.gumraze.drive.drive_backend.auth.constants.AuthProvider;
import com.gumraze.drive.drive_backend.auth.oauth.OAuthUserInfo;

import java.util.Optional;

public interface UserAuthRepository {

    /**
 * Locate the internal user ID associated with the given authentication provider and provider-specific user ID.
 *
 * @param provider the external authentication provider
 * @param providerUserId the identifier assigned to the user by the provider
 * @return an Optional containing the internal user ID if a mapping exists, or an empty Optional otherwise
 */
Optional<Long> findUserId(AuthProvider provider, String providerUserId);

    /**
 * Persist a mapping from an external provider account to an internal user and store the provider's profile data.
 *
 * Persists the association between the given authentication provider's user (described by `userInfo`) and the local
 * `userId`, and stores or updates profile information supplied in `userInfo`.
 *
 * @param provider the authentication provider (e.g., GOOGLE, GITHUB)
 * @param userInfo structured OAuth user information from the provider
 * @param userId internal application user identifier to associate with the provider account
 */
void save(AuthProvider provider, OAuthUserInfo userInfo, Long userId);

    /**
 * Update the stored profile information for a user from the given OAuth provider.
 *
 * @param provider the authentication provider associated with the user (e.g., Google, GitHub)
 * @param userInfo the provider-specific user profile data used to update the repository record
 */
void updateProfile(AuthProvider provider, OAuthUserInfo userInfo);
}