package com.gumraze.drive.drive_backend.user.service;

import com.gumraze.drive.drive_backend.user.repository.JpaUserAuthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserAuthNicknameProvider implements UserNicknameProvider {

    private final JpaUserAuthRepository jpaUserAuthRepository;

    @Override
    public Optional<String> findNicknameByUserId(Long userId) {
        return jpaUserAuthRepository
                .findFirstByUser_IdOrderByUpdatedAtDesc(userId)
                .map(userAuth -> userAuth.getNickname())
                .filter(nickname -> nickname != null && !nickname.isBlank());
    }

}
