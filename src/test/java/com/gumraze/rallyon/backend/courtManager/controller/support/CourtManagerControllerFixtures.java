package com.gumraze.rallyon.backend.courtManager.controller.support;

import com.gumraze.rallyon.backend.courtManager.constants.GameStatus;
import com.gumraze.rallyon.backend.courtManager.constants.GameType;
import com.gumraze.rallyon.backend.courtManager.constants.MatchRecordMode;
import com.gumraze.rallyon.backend.courtManager.dto.FreeGameDetailResponse;
import com.gumraze.rallyon.backend.courtManager.dto.FreeGameParticipantDetailResponse;
import com.gumraze.rallyon.backend.courtManager.dto.FreeGameParticipantResponse;
import com.gumraze.rallyon.backend.courtManager.dto.FreeGameParticipantsResponse;
import com.gumraze.rallyon.backend.user.constants.Gender;
import com.gumraze.rallyon.backend.user.constants.Grade;
import com.gumraze.rallyon.backend.user.constants.GradeType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

public final class CourtManagerControllerFixtures {

    private static final LocalDateTime DEFAULT_SCHEDULED_AT =
            LocalDateTime.of(2026, 4, 1, 15, 10);

    private CourtManagerControllerFixtures() {
    }

    public static RequestPostProcessor authenticatedUser(UUID accountId) {
        return authentication(
                new UsernamePasswordAuthenticationToken(
                        accountId,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                )
        );
    }

    public static FreeGameDetailResponse freeGameDetailResponse(UUID organizerId, UUID gameId) {
        return new FreeGameDetailResponse(
                gameId,
                "자유게임",
                GameType.FREE,
                GameStatus.NOT_STARTED,
                MatchRecordMode.STATUS_ONLY,
                GradeType.NATIONAL,
                2,
                2,
                organizerId,
                null,
                DEFAULT_SCHEDULED_AT,
                "잠실 배드민턴장"
        );
    }

    public static FreeGameParticipantResponse participantResponse(UUID participantId, UUID accountId, String displayName) {
        return new FreeGameParticipantResponse(
                participantId,
                accountId,
                displayName,
                Gender.MALE,
                Grade.ROOKIE,
                30,
                null,
                null,
                null,
                null
        );
    }

    public static FreeGameParticipantResponse participantResponseWithStats(
            FreeGameParticipantResponse participant,
            int assignedMatchCount,
            int completedMatchCount,
            int winCount,
            int lossCount
    ) {
        return new FreeGameParticipantResponse(
                participant.participantId(),
                participant.accountId(),
                participant.displayName(),
                participant.gender(),
                participant.grade(),
                participant.ageGroup(),
                assignedMatchCount,
                completedMatchCount,
                winCount,
                lossCount
        );
    }

    public static FreeGameParticipantsResponse participantsResponse(
            UUID gameId,
            List<FreeGameParticipantResponse> participants
    ) {
        return new FreeGameParticipantsResponse(gameId, MatchRecordMode.WINNER_ONLY, participants);
    }

    public static FreeGameParticipantDetailResponse participantDetailResponse(
            UUID gameId,
            UUID participantId,
            UUID accountId,
            String displayName
    ) {
        return new FreeGameParticipantDetailResponse(
                gameId,
                participantId,
                accountId,
                displayName,
                Gender.MALE,
                Grade.ROOKIE,
                30
        );
    }
}
