package com.gumraze.drive.drive_backend.user.service;

import com.gumraze.drive.drive_backend.user.dto.UserSearchResponse;
import com.gumraze.drive.drive_backend.user.entity.UserProfile;
import com.gumraze.drive.drive_backend.user.repository.UserProfileRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserSearchServiceImpl implements UserSearchService {

    private final UserProfileRepository userProfileRepository;

    @Override
    public Page<UserSearchResponse> searchByNickname(
            String nickname,
            Pageable pageable
    ) {
        if (nickname == null || nickname.isEmpty()) {
            throw new IllegalArgumentException("닉네임이 없습니다.");
        }

        Page<UserProfile> users = userProfileRepository.findByNicknameContaining(nickname, pageable);

        return users.map(user -> UserSearchResponse.builder()
                        .userId(user.getUser().getId())
                        .nickname(user.getNickname())
                        .tag(user.getTag())
                        .profileImageUrl(user.getProfileImageUrl())
                        .build());
    }

    @Override
    public Optional<UserSearchResponse> searchByNicknameAndTag(String nickname, String tags) {

        if (nickname == null || nickname.isEmpty()) {
            throw new IllegalArgumentException("닉네임이 없습니다.");
        }

        if (tags == null || tags.isEmpty()) {
            throw new IllegalArgumentException("태그가 없습니다.");
        }

        String normalizedTag = tags.replaceAll("[^A-Za-z0-9]", "")
                .toUpperCase(Locale.ROOT);

        return userProfileRepository.findByNicknameAndTag(nickname, normalizedTag)
                .map(user -> UserSearchResponse.builder()
                        .userId(user.getUser().getId())
                        .nickname(user.getNickname())
                        .tag(user.getTag())
                        .profileImageUrl(user.getProfileImageUrl())
                        .build());
    }
}