package com.gumraze.drive.drive_backend.user.service;

import com.gumraze.drive.drive_backend.user.entity.User;
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
}
