package com.gumraze.drive.drive_backend.courtManager.dto;

import com.gumraze.drive.drive_backend.courtManager.constants.GameStatus;
import com.gumraze.drive.drive_backend.courtManager.constants.GameType;
import com.gumraze.drive.drive_backend.courtManager.constants.MatchRecordMode;
import com.gumraze.drive.drive_backend.courtManager.entity.FreeGameSetting;
import com.gumraze.drive.drive_backend.courtManager.entity.Game;
import com.gumraze.drive.drive_backend.user.constants.GradeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
@AllArgsConstructor
public class FreeGameDetailResponse {
    private Long gameId;
    private String title;
    private GameType gameType;
    private GameStatus gameStatus;
    private MatchRecordMode matchRecordMode;
    private GradeType gradeType;
    private Integer courtCount;
    private Integer roundCount;
    private Long organizerId;
    private String shareCode;

    public static FreeGameDetailResponse from(Game game, FreeGameSetting setting) {
        return FreeGameDetailResponse.builder()
                .gameId(game.getId())
                .title(game.getTitle())
                .gameType(game.getGameType())
                .gameStatus(game.getGameStatus())
                .matchRecordMode(game.getMatchRecordMode())
                .gradeType(game.getGradeType())
                .courtCount(setting.getCourtCount())
                .roundCount(setting.getRoundCount())
                .organizerId(game.getOrganizer().getId())
                .shareCode(null)
                .build();
    }
}


