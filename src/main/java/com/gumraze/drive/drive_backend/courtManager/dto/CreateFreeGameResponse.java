package com.gumraze.drive.drive_backend.courtManager.dto;

import com.gumraze.drive.drive_backend.courtManager.entity.CourtGame;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateFreeGameResponse {
    private Long gameId;

    public static CreateFreeGameResponse from(CourtGame courtGame) {
        CreateFreeGameResponse response = new CreateFreeGameResponse();
        response.setGameId(courtGame.getId());
        return response;
    }
}
