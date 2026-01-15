package com.gumraze.drive.drive_backend.user.service;

import com.gumraze.drive.drive_backend.user.dto.UserSearchResponse;
import com.gumraze.drive.drive_backend.user.entity.UserProfile;
import com.gumraze.drive.drive_backend.user.repository.UserProfileRepository;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Locale;

@AllArgsConstructor
public class UserSearchServiceImpl implements UserSearchService {

    private final UserProfileRepository userProfileRepository;

    @Override
    public List<UserSearchResponse> searchByNickname(String nickname) {
        if (nickname == null || nickname.isEmpty()) {
            throw new IllegalArgumentException("닉네임이 없습니다.");
        }

        List<UserProfile> users = userProfileRepository.findByNicknameContaining(nickname);

        return users.stream()
                .map(user -> UserSearchResponse.builder()
                        .userId(user.getUser().getId())
                        .nickname(user.getNickname())
                        .tag(user.getTag())
                        .profileImageUrl(user.getProfileImageUrl())
                        .build())
                .toList();
    }

    @Override
    public UserSearchResponse searchByNicknameAndTag(String nickname, String tags) {

        if (nickname == null || nickname.isEmpty()) {
            throw new IllegalArgumentException("닉네임이 없습니다.");
        }

        if (tags == null || tags.isEmpty()) {
            throw new IllegalArgumentException("태그가 없습니다.");
        }

        String normalizedTag = tags.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);

        UserProfile users = userProfileRepository.findByNicknameAndTag(nickname, normalizedTag)
                .orElseThrow(() -> new IllegalArgumentException("유저가 없습니다."));


        return UserSearchResponse.builder()
                .userId(users.getUser().getId())
                .nickname(users.getNickname())
                .tag(users.getTag())
                .profileImageUrl(users.getProfileImageUrl())
                .build();
    }
}