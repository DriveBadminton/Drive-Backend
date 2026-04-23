package com.gumraze.rallyon.backend.courtManager.domain;

import com.gumraze.rallyon.backend.courtManager.constants.MatchRecordMode;
import com.gumraze.rallyon.backend.courtManager.constants.MatchResult;
import com.gumraze.rallyon.backend.courtManager.constants.MatchStatus;
import com.gumraze.rallyon.backend.courtManager.constants.RoundStatus;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGameMatch;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGameRound;
import com.gumraze.rallyon.backend.courtManager.entity.GameParticipant;
import com.gumraze.rallyon.backend.courtManager.support.CourtManagerTestFixtures;
import com.gumraze.rallyon.backend.user.constants.Gender;
import com.gumraze.rallyon.backend.user.constants.Grade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ParticipantStatsCalculatorTest {

    @Test
    @DisplayName("WINNER_ONLY 모드에서는 assigned, completed, win, loss를 계산한다")
    void calculate_counts_all_stats_in_result_mode() {
        UUID organizerAccountId = UUID.randomUUID();
        FreeGame freeGame = CourtManagerTestFixtures.freeGame(UUID.randomUUID(), organizerAccountId, MatchRecordMode.WINNER_ONLY);
        FreeGameRound round = CourtManagerTestFixtures.round(freeGame, UUID.randomUUID(), 1, RoundStatus.NOT_STARTED);

        GameParticipant participant1 = participant(freeGame, "서승재");
        GameParticipant participant2 = participant(freeGame, "김원호");
        GameParticipant participant3 = participant(freeGame, "안세영");
        GameParticipant participant4 = participant(freeGame, "정나은");

        FreeGameMatch completedMatch = CourtManagerTestFixtures.match(
                round,
                UUID.randomUUID(),
                1,
                participant1,
                participant2,
                participant3,
                participant4,
                MatchStatus.COMPLETED,
                MatchResult.TEAM_A_WIN,
                true
        );
        FreeGameMatch plannedMatch = CourtManagerTestFixtures.match(
                round,
                UUID.randomUUID(),
                2,
                participant1,
                null,
                participant3,
                null,
                MatchStatus.NOT_STARTED,
                MatchResult.NULL,
                true
        );

        Map<UUID, ParticipantStatsCalculator.ParticipantStats> result = ParticipantStatsCalculator.calculate(
                MatchRecordMode.WINNER_ONLY,
                List.of(participant1, participant2, participant3, participant4),
                List.of(completedMatch, plannedMatch)
        );

        assertThat(result.get(participant1.getId()).assignedMatchCount()).isEqualTo(2);
        assertThat(result.get(participant1.getId()).completedMatchCount()).isEqualTo(1);
        assertThat(result.get(participant1.getId()).winCount()).isEqualTo(1);
        assertThat(result.get(participant1.getId()).lossCount()).isEqualTo(0);

        assertThat(result.get(participant3.getId()).assignedMatchCount()).isEqualTo(2);
        assertThat(result.get(participant3.getId()).completedMatchCount()).isEqualTo(1);
        assertThat(result.get(participant3.getId()).winCount()).isEqualTo(0);
        assertThat(result.get(participant3.getId()).lossCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("STATUS_ONLY 모드에서는 승패를 계산하지 않는다")
    void calculate_does_not_count_win_loss_in_status_only_mode() {
        UUID organizerAccountId = UUID.randomUUID();
        FreeGame freeGame = CourtManagerTestFixtures.freeGame(UUID.randomUUID(), organizerAccountId, MatchRecordMode.STATUS_ONLY);
        FreeGameRound round = CourtManagerTestFixtures.round(freeGame, UUID.randomUUID(), 1, RoundStatus.NOT_STARTED);

        GameParticipant participant1 = participant(freeGame, "서승재");
        GameParticipant participant2 = participant(freeGame, "김원호");
        GameParticipant participant3 = participant(freeGame, "안세영");
        GameParticipant participant4 = participant(freeGame, "정나은");

        FreeGameMatch completedMatch = CourtManagerTestFixtures.match(
                round,
                UUID.randomUUID(),
                1,
                participant1,
                participant2,
                participant3,
                participant4,
                MatchStatus.COMPLETED,
                MatchResult.TEAM_A_WIN,
                true
        );

        Map<UUID, ParticipantStatsCalculator.ParticipantStats> result = ParticipantStatsCalculator.calculate(
                MatchRecordMode.STATUS_ONLY,
                List.of(participant1, participant2, participant3, participant4),
                List.of(completedMatch)
        );

        assertThat(result.get(participant1.getId()).assignedMatchCount()).isEqualTo(1);
        assertThat(result.get(participant1.getId()).completedMatchCount()).isEqualTo(1);
        assertThat(result.get(participant1.getId()).winCount()).isZero();
        assertThat(result.get(participant1.getId()).lossCount()).isZero();
    }

    @Test
    @DisplayName("null 슬롯은 통계 계산에서 무시한다")
    void calculate_ignores_null_slots() {
        UUID organizerAccountId = UUID.randomUUID();
        FreeGame freeGame = CourtManagerTestFixtures.freeGame(UUID.randomUUID(), organizerAccountId, MatchRecordMode.WINNER_ONLY);
        FreeGameRound round = CourtManagerTestFixtures.round(freeGame, UUID.randomUUID(), 1, RoundStatus.NOT_STARTED);

        GameParticipant participant1 = participant(freeGame, "서승재");

        FreeGameMatch sparseMatch = CourtManagerTestFixtures.match(
                round,
                UUID.randomUUID(),
                1,
                participant1,
                null,
                null,
                null,
                MatchStatus.NOT_STARTED,
                MatchResult.NULL,
                true
        );

        Map<UUID, ParticipantStatsCalculator.ParticipantStats> result = ParticipantStatsCalculator.calculate(
                MatchRecordMode.WINNER_ONLY,
                List.of(participant1),
                List.of(sparseMatch)
        );

        assertThat(result.get(participant1.getId()).assignedMatchCount()).isEqualTo(1);
        assertThat(result.get(participant1.getId()).completedMatchCount()).isZero();
        assertThat(result.get(participant1.getId()).winCount()).isZero();
        assertThat(result.get(participant1.getId()).lossCount()).isZero();
    }

    private GameParticipant participant(FreeGame freeGame, String name) {
        return CourtManagerTestFixtures.participant(
                freeGame,
                UUID.randomUUID(),
                UUID.randomUUID(),
                name,
                name,
                Gender.MALE,
                Grade.A,
                20,
                LocalDateTime.now()
        );
    }
}
