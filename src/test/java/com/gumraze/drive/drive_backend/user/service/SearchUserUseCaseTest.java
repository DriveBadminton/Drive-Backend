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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
        Pageable pageable = PageRequest.of(0, 20);

        User user = User.builder().id(1L).build();
        UserProfile profile = UserProfile.builder()
                .user(user)
                .nickname(nickname)
                .tag("AB12")
                .build();

        Page<UserProfile> users = new PageImpl<>(List.of(profile), pageable, 1);
        when(userProfileRepository.findByNicknameContaining(nickname, pageable))
                .thenReturn(users);

        // when
        Page<UserSearchResponse> result =
                userSearchService.searchByNickname(nickname, pageable);

        // then
        verify(userProfileRepository).findByNicknameContaining(nickname, pageable);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getNickname()).isEqualTo(nickname);
        assertThat(result.getContent().getFirst().getUserId()).isEqualTo(1L);
        assertThat(result.getContent().getFirst().getTag()).isEqualTo("AB12");
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
        Optional<UserSearchResponse> response =
                userSearchService.searchByNicknameAndTag(nickname, tagInput);

        // then
        verify(userProfileRepository).findByNicknameAndTag(nickname, normalizedTag);
        assertThat(response).isPresent();
        assertThat(response.get().getUserId()).isEqualTo(userId);
        assertThat(response.get().getNickname()).isEqualTo(nickname);
        assertThat(response.get().getTag()).isEqualTo(normalizedTag);
    }

    @Test
    @DisplayName("닉네임 검색은 대소문자를 구분한다.")
    void search_by_nickname_is_case_sensitive() {
        // given
        String nickname = "Kim";
        Pageable pageable = PageRequest.of(0, 20);

        when(userProfileRepository.findByNicknameContaining(nickname, pageable))
                .thenReturn(Page.empty(pageable));

        // when
        userSearchService.searchByNickname(nickname, pageable);

        // then
        verify(userProfileRepository).findByNicknameContaining(nickname, pageable);
    }
}
