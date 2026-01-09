package com.gumraze.drive.drive_backend.courtManager.dto;

import com.gumraze.drive.drive_backend.courtManager.entity.Game;
import lombok.Builder;

@Builder
public class UpdateFreeGameResponse {
    private Long gameId;

    public static UpdateFreeGameResponse from(Game game) {
        return UpdateFreeGameResponse.builder()
                .gameId(game.getId())
                .build();
    }
}
