package com.gumraze.drive.drive_backend.courtManager.entity;

import com.gumraze.drive.drive_backend.courtManager.constants.GameStatus;
import com.gumraze.drive.drive_backend.courtManager.constants.GameType;
import com.gumraze.drive.drive_backend.courtManager.constants.MatchRecordMode;
import com.gumraze.drive.drive_backend.user.constants.GradeType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
@Builder
@AllArgsConstructor
@Table(name = "court_games")
public class CourtGame {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;


    @Column(name = "organizer_id", nullable = false)
    private Long organizerId;     // 게임을 생성한 유저 id

    @Enumerated(EnumType.STRING)
    @Column(name = "grade_type", nullable = false)
    private GradeType gradeType;        // 참가자들의 급수 형식

    @Enumerated(EnumType.STRING)
    @Column(name = "game_type", nullable = false)
    private GameType gameType;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "game_status", nullable = false)
    private GameStatus gameStatus = GameStatus.NOT_STARTED;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "match_record_mode", nullable = false)
    private MatchRecordMode matchRecordMode = MatchRecordMode.STATUS_ONLY;

    @Column(name = "share_code", length = 64)
    private String shareCode;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected CourtGame() {}
}
