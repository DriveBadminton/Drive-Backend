package com.gumraze.drive.drive_backend.courtManager.dto;

import com.gumraze.drive.drive_backend.courtManager.entity.FreeGame;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateFreeGameResponse {
    private Long gameId;

    public static UpdateFreeGameResponse from(FreeGame freeGame) {
        return UpdateFreeGameResponse.builder()
                .gameId(freeGame.getId())
                .build();
    }
}
