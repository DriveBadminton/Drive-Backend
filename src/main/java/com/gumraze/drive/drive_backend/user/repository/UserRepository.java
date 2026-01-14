package com.gumraze.drive.drive_backend.user.repository;

import com.gumraze.drive.drive_backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Long createUser();

    Optional<User> findById(Long id);

    boolean existsById(Long userId);
}
