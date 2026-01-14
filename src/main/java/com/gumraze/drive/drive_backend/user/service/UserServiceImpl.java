package com.gumraze.drive.drive_backend.user.service;

import com.gumraze.drive.drive_backend.user.constants.UserStatus;
import com.gumraze.drive.drive_backend.user.dto.UserMeResponse;
import com.gumraze.drive.drive_backend.user.entity.User;
import com.gumraze.drive.drive_backend.user.entity.UserProfile;
import com.gumraze.drive.drive_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }

    @Override
    public UserMeResponse getUserMe(Long userId) {

        // 사용자 조회, 없으면 실패 처리
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 프로필 조회, 없으면 null 반환
        UserProfile profile = null;
        if (user.getStatus() == UserStatus.ACTIVE) {
            profile = userProfileRepository.findByUserId(userId).orElse(null);
        }

        // 사용자 상태 + 프로필 정보가 있으면 DTO로 반환
        return UserMeResponse.from(user, profile);
    }
}
