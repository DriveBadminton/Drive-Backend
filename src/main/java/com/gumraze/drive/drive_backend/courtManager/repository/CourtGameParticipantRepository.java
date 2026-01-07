package com.gumraze.drive.drive_backend.courtManager.repository;

import com.gumraze.drive.drive_backend.courtManager.entity.CourtGameParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourtGameParticipantRepository extends JpaRepository<CourtGameParticipant, Long> {
}
