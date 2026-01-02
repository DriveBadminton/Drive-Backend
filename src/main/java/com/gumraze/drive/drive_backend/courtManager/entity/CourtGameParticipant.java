package com.gumraze.drive.drive_backend.courtManager.entity;

import com.gumraze.drive.drive_backend.user.constants.Gender;
import com.gumraze.drive.drive_backend.user.constants.Grade;
import com.gumraze.drive.drive_backend.user.entity.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "court_game_participants",
        uniqueConstraints = @UniqueConstraint(columnNames = {"game_id", "user_id"})
)
public class CourtGameParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private CourtGame game;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Grade grade;

    @Column(name = "age_group", nullable = false)
    private Integer ageGroup;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected CourtGameParticipant() {}
}
