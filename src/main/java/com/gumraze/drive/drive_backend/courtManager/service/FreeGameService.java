package com.gumraze.drive.drive_backend.courtManager.service;

import com.gumraze.drive.drive_backend.courtManager.dto.CreateFreeGameRequest;
import com.gumraze.drive.drive_backend.courtManager.dto.CreateFreeGameResponse;

public interface FreeGameService {
    CreateFreeGameResponse createFreeGame(Long userId, CreateFreeGameRequest request);
}
