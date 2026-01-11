package com.gumraze.drive.drive_backend.courtManager.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class FreeGameRoundMatchResponse {
    Long gameId;
    List<FreeGameRoundResponse> rounds;

    public static FreeGameRoundMatchResponse from(Long gameId, List<FreeGameRoundResponse> rounds) {
        return FreeGameRoundMatchResponse.builder()
                .gameId(gameId)
                .rounds(rounds)
                .build();
    }
}
