package com.gumraze.drive.drive_backend.courtManager.entity;

import com.gumraze.drive.drive_backend.user.entity.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "game_managers",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"freegame_id", "user_id"}
        )
)
public class GameManager {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "freegame_id", nullable = false)
    private FreeGame freeGame;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    protected GameManager() {}
}
