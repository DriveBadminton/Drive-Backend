package com.gumraze.drive.drive_backend.courtManager.repository;

import com.gumraze.drive.drive_backend.courtManager.entity.FreeGameMatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FreeGameMatchRepository extends JpaRepository<FreeGameMatch, Long> {
    List<FreeGameMatch> findByRoundIdInOrderByCourtNumber(List<Long> roundIds);
}

