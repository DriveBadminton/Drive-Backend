package com.gumraze.drive.drive_backend.courtManager.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UpdateFreeGameRoundMatchResponse {
    private final Long gameId;

    public static UpdateFreeGameRoundMatchResponse from(Long gameId) {
        return new UpdateFreeGameRoundMatchResponse(gameId);
    }
}
