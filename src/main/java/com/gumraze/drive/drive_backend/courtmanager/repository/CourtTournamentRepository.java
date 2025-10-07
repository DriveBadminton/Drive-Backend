package com.gumraze.drive.drive_backend.courtmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gumraze.drive.drive_backend.courtmanager.entity.CourtTournament;

public interface CourtTournamentRepository extends JpaRepository<CourtTournament, Long> {
}
