package com.gumraze.drive.drive_backend.courtManager.repository;

import com.gumraze.drive.drive_backend.courtManager.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourtGameRepository extends JpaRepository<Game, Long> {
}
