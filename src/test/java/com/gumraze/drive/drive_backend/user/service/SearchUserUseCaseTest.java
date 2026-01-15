package com.gumraze.drive.drive_backend.user.service;

import com.gumraze.drive.drive_backend.user.dto.UserSearchResponse;
import com.gumraze.drive.drive_backend.user.entity.User;
import com.gumraze.drive.drive_backend.user.entity.UserProfile;
import com.gumraze.drive.drive_backend.user.repository.UserProfileRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SearchUserUseCaseTest {

    @Mock
    UserProfileRepository userProfileRepository;

    @InjectMocks
    UserSearchServiceImpl userSearchService;

    @Test
    @DisplayName("nickname 포함 검색으로 사용자 검색")
    void search_by_nickname_containing() {
        // given
        String nickname = "김대환";

        User user = User.builder().id(1L).build();
        UserProfile profile = UserProfile.builder()
                .user(user)
                .nickname(nickname)
                .tag("AB12")
                .build();

        when(userProfileRepository.findByNicknameContaining(nickname)).thenReturn(List.of(profile));

        // when
        List<UserSearchResponse> result = userSearchService.searchByNickname(nickname);

        // then
        verify(userProfileRepository).findByNicknameContaining(nickname);
        assertThat(result.getFirst().getNickname()).isEqualTo(nickname);
        assertThat(result.getFirst().getUserId()).isEqualTo(user.getId());
        assertThat(result.getFirst().getTag()).isEqualTo("AB12");
    }

    @Test
    @DisplayName("nickname과 tag를 정확하게 일치하도록 사용자 검색")
    void search_by_nickname_and_tag_exact_match() {
        // given
        String nickname = "김대환";
        String tagInput = "ab12";
        String normalizedTag = "AB12";
        Long userId = 1L;

        User user = User.builder()
                .id(userId)
                .build();

        UserProfile profile = UserProfile.builder()
                .user(user)
                .nickname(nickname)
                .tag(normalizedTag)
                .build();

        when(userProfileRepository.findByNicknameAndTag(nickname, normalizedTag))
                .thenReturn(Optional.of(profile));

        // when
        UserSearchResponse response =
                userSearchService.searchByNicknameAndTag(nickname, tagInput);

        // then
        verify(userProfileRepository).findByNicknameAndTag(nickname, normalizedTag);
        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getNickname()).isEqualTo(nickname);
        assertThat(response.getTag()).isEqualTo(normalizedTag);
    }

    @Test
    @DisplayName("닉네임 검색은 대소문자를 구분한다.")
    void search_by_nickname_is_case_sensitive() {
        // given
        String nickname = "Kim";

        when(userProfileRepository.findByNicknameContaining(nickname)).thenReturn(List.of());

        // when
        userSearchService.searchByNickname(nickname);

        // then
        verify(userProfileRepository).findByNicknameContaining(nickname);
    }
}
