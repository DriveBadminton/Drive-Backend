package com.gumraze.drive.drive_backend.courtManager.dto;

import com.gumraze.drive.drive_backend.courtManager.entity.FreeGame;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateFreeGameResponse {
    private Long gameId;

    public static CreateFreeGameResponse from(FreeGame freeGame) {
        CreateFreeGameResponse response = new CreateFreeGameResponse();
        response.setGameId(freeGame.getId());
        return response;
    }
}
