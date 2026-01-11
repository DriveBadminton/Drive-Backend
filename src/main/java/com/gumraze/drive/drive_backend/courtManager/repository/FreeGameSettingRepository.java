package com.gumraze.drive.drive_backend.courtManager.repository;

import com.gumraze.drive.drive_backend.courtManager.entity.FreeGameSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FreeGameSettingRepository extends JpaRepository<FreeGameSetting, Long> {
    Optional<FreeGameSetting> findByFreeGameId(Long freeGameId);
}
