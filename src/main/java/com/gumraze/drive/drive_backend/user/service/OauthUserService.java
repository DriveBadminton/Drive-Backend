package com.gumraze.drive.drive_backend.user.service;

import com.gumraze.drive.drive_backend.auth.constants.AuthProvider;
import com.gumraze.drive.drive_backend.user.entity.OauthUser;
import com.gumraze.drive.drive_backend.user.repository.OauthUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Service
@Transactional
public class OauthUserService {

    private final OauthUserRepository oauthUserRepository;

    public OauthUser saveOrUpdate(AuthProvider provider, String oauthId, String email, String nickname, String profileImageUrl) {
        return oauthUserRepository
            .findByOauthProviderAndOauthId(provider, oauthId)
            .map(existing -> existing.updateProfile(email, nickname, profileImageUrl))
            .orElseGet(() -> oauthUserRepository.save(
                OauthUser.builder()
                    .oauthProvider(provider)
                    .oauthId(oauthId)
                    .email(email)
                    .nickname(nickname)
                    .profileImageUrl(profileImageUrl)
                    .build()
            ));
    }

    @Transactional(readOnly = true)
    public Optional<OauthUser> findById(Long id) {
        return oauthUserRepository.findById(id);
    }
}
