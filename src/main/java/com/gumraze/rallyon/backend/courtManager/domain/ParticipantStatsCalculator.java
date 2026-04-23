package com.gumraze.rallyon.backend.courtManager.domain;

import com.gumraze.rallyon.backend.courtManager.constants.MatchRecordMode;
import com.gumraze.rallyon.backend.courtManager.constants.MatchResult;
import com.gumraze.rallyon.backend.courtManager.constants.MatchStatus;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGameMatch;
import com.gumraze.rallyon.backend.courtManager.entity.GameParticipant;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class ParticipantStatsCalculator {

    private ParticipantStatsCalculator() {
    }

    public static Map<UUID, ParticipantStats> calculate(
            MatchRecordMode matchRecordMode,
            List<GameParticipant> participants,
            List<FreeGameMatch> matches
    ) {
        Map<UUID, ParticipantStats> statsByParticipantId = new HashMap<>();
        for (GameParticipant participant : participants) {
            statsByParticipantId.put(participant.getId(), new ParticipantStats());
        }

        for (FreeGameMatch match : matches) {
            Set<UUID> matchParticipantIds = new HashSet<>();
            addParticipantId(matchParticipantIds, match.getTeamAPlayer1());
            addParticipantId(matchParticipantIds, match.getTeamAPlayer2());
            addParticipantId(matchParticipantIds, match.getTeamBPlayer1());
            addParticipantId(matchParticipantIds, match.getTeamBPlayer2());

            for (UUID participantId : matchParticipantIds) {
                ParticipantStats stats = statsByParticipantId.get(participantId);
                if (stats != null) {
                    stats.assignedMatchCount++;
                }
            }

            if (match.getMatchStatus() == MatchStatus.COMPLETED) {
                for (UUID participantId : matchParticipantIds) {
                    ParticipantStats stats = statsByParticipantId.get(participantId);
                    if (stats != null) {
                        stats.completedMatchCount++;
                    }
                }
            }

            if (hasWinLossRecord(matchRecordMode)) {
                applyWinLossCounts(match, statsByParticipantId);
            }
        }

        return statsByParticipantId;
    }

    private static void addParticipantId(Set<UUID> target, GameParticipant participant) {
        if (participant != null) {
            target.add(participant.getId());
        }
    }

    private static boolean hasWinLossRecord(MatchRecordMode matchRecordMode) {
        return matchRecordMode == MatchRecordMode.WINNER_ONLY
                || matchRecordMode == MatchRecordMode.SCORE;
    }

    private static void applyWinLossCounts(
            FreeGameMatch match,
            Map<UUID, ParticipantStats> statsByParticipantId
    ) {
        MatchResult result = match.getMatchResult();
        if (result != MatchResult.TEAM_A_WIN && result != MatchResult.TEAM_B_WIN) {
            return;
        }

        Set<UUID> teamAIds = new HashSet<>();
        addParticipantId(teamAIds, match.getTeamAPlayer1());
        addParticipantId(teamAIds, match.getTeamAPlayer2());

        Set<UUID> teamBIds = new HashSet<>();
        addParticipantId(teamBIds, match.getTeamBPlayer1());
        addParticipantId(teamBIds, match.getTeamBPlayer2());

        if (result == MatchResult.TEAM_A_WIN) {
            incrementWinCounts(teamAIds, statsByParticipantId);
            incrementLossCounts(teamBIds, statsByParticipantId);
            return;
        }

        incrementWinCounts(teamBIds, statsByParticipantId);
        incrementLossCounts(teamAIds, statsByParticipantId);
    }

    private static void incrementWinCounts(Set<UUID> participantIds, Map<UUID, ParticipantStats> statsByParticipantId) {
        for (UUID participantId : participantIds) {
            ParticipantStats stats = statsByParticipantId.get(participantId);
            if (stats != null) {
                stats.winCount++;
            }
        }
    }

    private static void incrementLossCounts(Set<UUID> participantIds, Map<UUID, ParticipantStats> statsByParticipantId) {
        for (UUID participantId : participantIds) {
            ParticipantStats stats = statsByParticipantId.get(participantId);
            if (stats != null) {
                stats.lossCount++;
            }
        }
    }

    public static final class ParticipantStats {
        private int assignedMatchCount;
        private int completedMatchCount;
        private int winCount;
        private int lossCount;

        public int assignedMatchCount() {
            return assignedMatchCount;
        }

        public int completedMatchCount() {
            return completedMatchCount;
        }

        public int winCount() {
            return winCount;
        }

        public int lossCount() {
            return lossCount;
        }
    }
}
