package com.gumraze.drive.drive_backend.courtManager.service;

import com.gumraze.drive.drive_backend.courtManager.dto.CreateFreeGameRequest;
import com.gumraze.drive.drive_backend.courtManager.dto.CreateFreeGameResponse;
import com.gumraze.drive.drive_backend.courtManager.repository.CourtGameRepository;

public class FreeGameServiceImpl implements FreeGameService {

    private final CourtGameRepository courtGameRepository;

    public FreeGameServiceImpl(CourtGameRepository courtGameRepository) {
        this.courtGameRepository = courtGameRepository;
    }

    @Override
    public CreateFreeGameResponse createFreeGame(CreateFreeGameRequest request) {
        return null;
    }
}
