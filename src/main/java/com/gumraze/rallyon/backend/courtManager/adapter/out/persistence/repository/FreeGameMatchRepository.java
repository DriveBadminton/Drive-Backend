package com.gumraze.rallyon.backend.courtManager.adapter.out.persistence.repository;

import com.gumraze.rallyon.backend.courtManager.entity.FreeGameMatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FreeGameMatchRepository extends JpaRepository<FreeGameMatch, UUID> {
    List<FreeGameMatch> findByRoundIdInOrderByCourtNumber(List<UUID> roundIds);

    Optional<FreeGameMatch> findByRoundFreeGameIdAndRoundRoundNumberAndCourtNumber(
            UUID gameId,
            Integer roundNumber,
            Integer courtNumber
    );

    void deleteByRoundId(UUID id);
}
