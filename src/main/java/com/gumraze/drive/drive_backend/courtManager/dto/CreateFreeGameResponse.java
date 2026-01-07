package com.gumraze.drive.drive_backend.courtManager.dto;

import com.gumraze.drive.drive_backend.courtManager.entity.Game;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateFreeGameResponse {
    private Long gameId;

    public static CreateFreeGameResponse from(Game game) {
        CreateFreeGameResponse response = new CreateFreeGameResponse();
        response.setGameId(game.getId());
        return response;
    }
}
