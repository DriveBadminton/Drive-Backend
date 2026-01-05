package com.gumraze.drive.drive_backend.courtManager.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "court_free_game_settings")
public class CourtFreeGameSetting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false, unique = true)
    private CourtGame game;

    @Column(name = "court_count", nullable = false)
    private Integer courtCount;

    @Column(name = "round_count", nullable = false)
    private Integer roundCount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected CourtFreeGameSetting() {}
}
