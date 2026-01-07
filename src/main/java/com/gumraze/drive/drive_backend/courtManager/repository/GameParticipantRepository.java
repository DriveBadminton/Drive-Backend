package com.gumraze.drive.drive_backend.courtManager.repository;

import com.gumraze.drive.drive_backend.courtManager.entity.GameParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameParticipantRepository extends JpaRepository<GameParticipant, Long> {
}
