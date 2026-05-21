package com.gumraze.rallyon.backend.courtManager.entity;

import com.gumraze.rallyon.backend.common.persistence.MutableAuditEntity;
import com.gumraze.rallyon.backend.courtManager.constants.MatchResult;
import com.gumraze.rallyon.backend.courtManager.constants.MatchStatus;
import com.gumraze.rallyon.backend.courtManager.constants.MatchType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "free_game_match",
        uniqueConstraints = @UniqueConstraint(columnNames = {"round_id", "court_number"})
)
public class FreeGameMatch extends MutableAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_id", nullable = false)
    private FreeGameRound round;

    @Column(name = "court_number", nullable = false)
    private Integer courtNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_a_player1_id")
    private GameParticipant teamAPlayer1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_a_player2_id")
    private GameParticipant teamAPlayer2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_b_player1_id")
    private GameParticipant teamBPlayer1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_b_player2_id")
    private GameParticipant teamBPlayer2;

    @Enumerated(EnumType.STRING)
    private MatchType matchType;

    @Enumerated(EnumType.STRING)
    private MatchStatus matchStatus;

    @Enumerated(EnumType.STRING)
    private MatchResult matchResult;

    @Column(name = "team_a_score")
    private Integer teamAScore;

    @Column(name = "team_b_score")
    private Integer teamBScore;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;       // 코트 삭제 시 삭제된 코트를 표시하기 위함, 소프트 삭제용

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected FreeGameMatch() {
    }

    public static FreeGameMatch create(
            FreeGameRound round,
            Integer courtNumber,
            GameParticipant teamAPlayer1,
            GameParticipant teamAPlayer2,
            GameParticipant teamBPlayer1,
            GameParticipant teamBPlayer2,
            MatchType matchType,
            MatchStatus matchStatus,
            MatchResult matchResult,
            boolean isActive
    ) {
        FreeGameMatch match = new FreeGameMatch();
        match.round = round;
        match.courtNumber = courtNumber;
        match.teamAPlayer1 = teamAPlayer1;
        match.teamAPlayer2 = teamAPlayer2;
        match.teamBPlayer1 = teamBPlayer1;
        match.teamBPlayer2 = teamBPlayer2;
        match.matchType = matchType;
        match.matchStatus = matchStatus;
        match.matchResult = matchResult;
        match.isActive = isActive;
        return match;
    }

    public UUID getId() {
        return id;
    }

    public FreeGameRound getRound() {
        return round;
    }

    public Integer getCourtNumber() {
        return courtNumber;
    }

    public GameParticipant getTeamAPlayer1() {
        return teamAPlayer1;
    }

    public GameParticipant getTeamAPlayer2() {
        return teamAPlayer2;
    }

    public GameParticipant getTeamBPlayer1() {
        return teamBPlayer1;
    }

    public GameParticipant getTeamBPlayer2() {
        return teamBPlayer2;
    }

    public MatchType getMatchType() {
        return matchType;
    }

    public MatchStatus getMatchStatus() {
        return matchStatus;
    }

    public MatchResult getMatchResult() {
        return matchResult;
    }

    public Integer getTeamAScore() {
        return teamAScore;
    }

    public Integer getTeamBScore() {
        return teamBScore;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void start() {
        if (matchStatus != MatchStatus.NOT_STARTED) {
            throw new IllegalStateException("미진행 매치만 시작할 수 있습니다.");
        }
        matchStatus = MatchStatus.IN_PROGRESS;
    }

    public void completeStatusOnly() {
        complete(null, null, null);
    }

    public void completeWithWinner(MatchResult winner) {
        if (winner != MatchResult.TEAM_A_WIN && winner != MatchResult.TEAM_B_WIN) {
            throw new IllegalArgumentException("승자 기록은 TEAM_A_WIN 또는 TEAM_B_WIN만 허용합니다.");
        }
        complete(winner, null, null);
    }

    public void completeWithScore(Integer teamAScore, Integer teamBScore) {
        if (teamAScore == null || teamBScore == null) {
            throw new IllegalArgumentException("점수 기록은 양 팀 점수가 모두 필요합니다.");
        }
        if (teamAScore < 0 || teamBScore < 0) {
            throw new IllegalArgumentException("점수는 0 이상이어야 합니다.");
        }
        if (teamAScore.equals(teamBScore)) {
            throw new IllegalArgumentException("점수 기록에서는 동점을 허용하지 않습니다.");
        }
        complete(
                teamAScore > teamBScore ? MatchResult.TEAM_A_WIN : MatchResult.TEAM_B_WIN,
                teamAScore,
                teamBScore
        );
    }

    private void complete(MatchResult result, Integer teamAScore, Integer teamBScore) {
        if (matchStatus != MatchStatus.IN_PROGRESS) {
            throw new IllegalStateException("진행 중인 매치만 완료할 수 있습니다.");
        }
        matchStatus = MatchStatus.COMPLETED;
        matchResult = result;
        this.teamAScore = teamAScore;
        this.teamBScore = teamBScore;
    }

    @Override
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    protected void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    protected void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
