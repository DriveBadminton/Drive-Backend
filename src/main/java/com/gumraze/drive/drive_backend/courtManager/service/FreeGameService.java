package com.gumraze.drive.drive_backend.courtManager.service;

import com.gumraze.drive.drive_backend.courtManager.dto.*;

public interface FreeGameService {
    CreateFreeGameResponse createFreeGame(Long userId, CreateFreeGameRequest request);

    FreeGameDetailResponse getFreeGameDetail(Long userId, Long gameId);

    UpdateFreeGameResponse updateFreeGameInfo(Long userId, Long gameId, UpdateFreeGameRequest request);

    FreeGameRoundMatchResponse getFreeGameRoundMatchResponse(Long userId, Long gameId);
}
