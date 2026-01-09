package com.gumraze.drive.drive_backend.courtManager.service;

import com.gumraze.drive.drive_backend.courtManager.dto.CreateFreeGameRequest;
import com.gumraze.drive.drive_backend.courtManager.dto.CreateFreeGameResponse;
import com.gumraze.drive.drive_backend.courtManager.dto.FreeGameDetailResponse;

public interface FreeGameService {
    CreateFreeGameResponse createFreeGame(Long userId, CreateFreeGameRequest request);

    FreeGameDetailResponse getFreeGameDetail(Long userId, Long gameId);
}
