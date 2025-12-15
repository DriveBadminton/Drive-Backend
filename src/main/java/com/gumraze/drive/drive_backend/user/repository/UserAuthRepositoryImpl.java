package com.gumraze.drive.drive_backend.user.repository;

import com.gumraze.drive.drive_backend.auth.constants.AuthProvider;
import com.gumraze.drive.drive_backend.auth.repository.UserAuthRepository;
import com.gumraze.drive.drive_backend.user.entity.User;
import com.gumraze.drive.drive_backend.user.entity.UserAuth;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserAuthRepositoryImpl implements UserAuthRepository {

    private final JpaUserAuthRepository jpaUserAuthRepository;
    private final JpaUserRepository jpaUserRepository;


    @Override
    public Optional<Long> findUserId(
            AuthProvider provider,
            String providerUserId
    ) {
        return jpaUserAuthRepository
                .findByProviderAndProviderUserId(provider, providerUserId)
                .map(userAuth -> userAuth.getUser().getId());
    }

    @Override
    public void save(
            AuthProvider provider,
            String providerUserId,
            Long userId
    ) {
        User user = jpaUserRepository
                .findById(userId)
                .orElseThrow();

        UserAuth userAuth =
                new UserAuth(user, provider, providerUserId);

        jpaUserAuthRepository.save(userAuth);
    }
}
