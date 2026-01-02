package com.gumraze.drive.drive_backend.courtManager.entity;

import com.gumraze.drive.drive_backend.courtManager.constants.MatchStatus;
import com.gumraze.drive.drive_backend.courtManager.constants.MatchType;
import com.gumraze.drive.drive_backend.courtManager.constants.WinnerTeam;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "court_free_game_matches",
        uniqueConstraints = @UniqueConstraint(columnNames = {"round_id", "court_number"})
)
public class CourtFreeGameMatch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_id", nullable = false)
    private CourtFreeGameRound round;

    @Column(name = "court_number", nullable = false)
    private Integer courtNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_a_player1_id", nullable = false)
    private CourtGameParticipant teamAPlayer1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_a_player2_id", nullable = false)
    private CourtGameParticipant teamAPlayer2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_b_player1_id", nullable = false)
    private CourtGameParticipant teamBPlayer1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_b_player2_id", nullable = false)
    private CourtGameParticipant teamBPlayer2;

    @Enumerated(EnumType.STRING)
    private MatchType matchType;

    @Enumerated(EnumType.STRING)
    private MatchStatus matchStatus;

    @Enumerated(EnumType.STRING)
    private WinnerTeam winnerTeam;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;       // 코트 삭제 시 삭제된 코트를 표시하기 위함, 소프트 삭제용

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
