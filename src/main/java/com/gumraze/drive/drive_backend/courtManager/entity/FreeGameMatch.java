package com.gumraze.drive.drive_backend.courtManager.entity;

import com.gumraze.drive.drive_backend.courtManager.constants.MatchResult;
import com.gumraze.drive.drive_backend.courtManager.constants.MatchStatus;
import com.gumraze.drive.drive_backend.courtManager.constants.MatchType;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(
        name = "free_game_match",
        uniqueConstraints = @UniqueConstraint(columnNames = {"round_id", "court_number"})
)
public class FreeGameMatch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;       // 코트 삭제 시 삭제된 코트를 표시하기 위함, 소프트 삭제용

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
