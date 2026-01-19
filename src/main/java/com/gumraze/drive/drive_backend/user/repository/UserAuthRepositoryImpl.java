package com.gumraze.drive.drive_backend.user.repository;

import com.gumraze.drive.drive_backend.auth.constants.AuthProvider;
import com.gumraze.drive.drive_backend.auth.oauth.OAuthUserInfo;
import com.gumraze.drive.drive_backend.auth.repository.UserAuthRepository;
import com.gumraze.drive.drive_backend.user.entity.User;
import com.gumraze.drive.drive_backend.user.entity.UserAuth;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserAuthRepositoryImpl implements UserAuthRepository {

    private final JpaUserAuthRepository jpaUserAuthRepository;
    private final UserRepository userRepository;

    @Override
    public Optional<Long> findUserId(AuthProvider provider, String providerUserId) {
        return jpaUserAuthRepository
                .findByProviderAndProviderUserId(provider, providerUserId)
                .map(userAuth -> userAuth.getUser().getId());
    }


    @Override
    public void save(AuthProvider provider, OAuthUserInfo userInfo, Long userId) {
        User user = userRepository
                .findById(userId)
                .orElseThrow();

        UserAuth userAuth = UserAuth.builder()
                .user(user)
                .provider(provider)
                .providerUserId(userInfo.getProviderUserId())
                .build();

        userAuth.setEmail(userInfo.getEmail());
        userAuth.setNickname(userInfo.getNickname());
        userAuth.setProfileImageUrl(userInfo.getProfileImageUrl());
        userAuth.setGender(userInfo.getGender());
        userAuth.setThumbnailImageUrl(userInfo.getThumbnailImageUrl());
        userAuth.setAgeRange(userInfo.getAgeRange());
        userAuth.setBirthday(userInfo.getBirthday());
        userAuth.setIsEmailVerified(userInfo.getEmailVerified());
        userAuth.setIsPhoneNumberVerified(userInfo.getPhoneNumberVerified());
        userAuth.setUpdatedAt(LocalDateTime.now());

        jpaUserAuthRepository.save(userAuth);
    }

    @Override
    public void updateProfile(AuthProvider provider, OAuthUserInfo userInfo) {
        // 동일 provider와 id가 있으면 최신 프로필로 갱신
        jpaUserAuthRepository.findByProviderAndProviderUserId(provider, userInfo.getProviderUserId())
                .ifPresent(userAuth -> {
                    userAuth.setEmail(userInfo.getEmail());
                    userAuth.setNickname(userInfo.getNickname());
                    userAuth.setProfileImageUrl(userInfo.getProfileImageUrl());
                    userAuth.setGender(userInfo.getGender());
                    userAuth.setThumbnailImageUrl(userInfo.getThumbnailImageUrl());
                    userAuth.setAgeRange(userInfo.getAgeRange());
                    userAuth.setBirthday(userInfo.getBirthday());
                    userAuth.setIsEmailVerified(userInfo.getEmailVerified());
                    userAuth.setIsPhoneNumberVerified(userInfo.getPhoneNumberVerified());
                    userAuth.setUpdatedAt(LocalDateTime.now());
                });
    }
}