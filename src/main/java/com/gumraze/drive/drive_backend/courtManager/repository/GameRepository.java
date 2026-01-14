package com.gumraze.drive.drive_backend.courtManager.repository;

import com.gumraze.drive.drive_backend.courtManager.entity.FreeGame;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<FreeGame, Long> {
}
