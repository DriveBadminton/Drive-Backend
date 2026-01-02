package com.gumraze.drive.drive_backend.courtManager.entity;

import com.gumraze.drive.drive_backend.courtManager.constants.GameStatus;
import com.gumraze.drive.drive_backend.courtManager.constants.GameType;
import com.gumraze.drive.drive_backend.courtManager.constants.MatchRecordMode;
import com.gumraze.drive.drive_backend.user.entity.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "court_games")
public class CourtGame {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

    @Enumerated(EnumType.STRING)
    @Column(name = "game_type", nullable = false)
    private GameType gameType;

    @Enumerated(EnumType.STRING)
    @Column(name = "game_status", nullable = false)
    private GameStatus gameStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "match_record_mode", nullable = false)
    private MatchRecordMode matchRecordMode;

    @Column(name = "share_code", length = 64)
    private String shareCode;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected CourtGame() {}
}
