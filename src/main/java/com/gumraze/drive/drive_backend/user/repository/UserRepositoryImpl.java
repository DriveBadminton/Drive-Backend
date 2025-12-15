package com.gumraze.drive.drive_backend.user.repository;

import com.gumraze.drive.drive_backend.user.constants.UserRole;
import com.gumraze.drive.drive_backend.user.constants.UserStatus;
import com.gumraze.drive.drive_backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final JpaUserRepository jpaUserRepository;


    @Override
    public Long createUser() {
        User user = new User(
                UserStatus.ACTIVE,
                UserRole.USER
        );
        return jpaUserRepository.save(user).getId();
    }
}
