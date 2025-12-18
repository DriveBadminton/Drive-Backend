package com.gumraze.drive.drive_backend.auth.entity;

import com.gumraze.drive.drive_backend.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "refresh_token",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "token_hash")
        }
)
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String tokenHash;

    private LocalDateTime expiredAt;
    private LocalDateTime createdAt;

    protected RefreshToken() {}

    public RefreshToken(
            User user,
            String tokenHash,
            LocalDateTime expiredAt
    ) {
        this.user = user;
        this.tokenHash = tokenHash;
        this.expiredAt = expiredAt;
        this.createdAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        return expiredAt.isBefore(LocalDateTime.now());
    }
}
