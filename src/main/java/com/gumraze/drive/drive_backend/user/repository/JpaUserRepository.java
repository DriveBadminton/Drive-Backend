package com.gumraze.drive.drive_backend.user.repository;

import com.gumraze.drive.drive_backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaUserRepository extends JpaRepository<User, Long> {

}
