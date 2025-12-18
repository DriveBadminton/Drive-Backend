package com.gumraze.drive.drive_backend.user.entity;

import com.gumraze.drive.drive_backend.auth.constants.AuthProvider;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(
    name = "user_auth",
    uniqueConstraints = {
            @UniqueConstraint(columnNames = {"provider", "provider_user_id"})
    }
)
public class UserAuth {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // User 1 <- N UserAuth(Kakao, Google, ... )
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider provider;

    @Column(name = "provider_user_id", nullable = false)
    private String providerUserId;

    private String email;
    private String nickname;
    private String profileImageUrl;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // JPA 기본 생성자
    protected UserAuth() {}

    // OAuth 로그인 시 사용하는 최소 생성자
    public UserAuth(
        User user,
        AuthProvider provider,
        String providerUserId
    ) {
        this.user = user;
        this.provider = provider;
        this.providerUserId = providerUserId;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
