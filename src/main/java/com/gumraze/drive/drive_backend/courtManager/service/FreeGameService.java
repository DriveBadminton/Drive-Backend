package com.gumraze.drive.drive_backend.courtManager.service;

import com.gumraze.drive.drive_backend.courtManager.dto.CreateFreeGameRequest;
import com.gumraze.drive.drive_backend.courtManager.dto.CreateFreeGameResponse;
import org.springframework.stereotype.Service;

@Service
public interface FreeGameService {
    CreateFreeGameResponse createFreeGame(Long userId, CreateFreeGameRequest request);
}
