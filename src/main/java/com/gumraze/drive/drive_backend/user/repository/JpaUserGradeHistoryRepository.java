package com.gumraze.drive.drive_backend.user.repository;

import com.gumraze.drive.drive_backend.user.entity.UserGradeHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaUserGradeHistoryRepository extends JpaRepository<UserGradeHistory, Long> {
}
