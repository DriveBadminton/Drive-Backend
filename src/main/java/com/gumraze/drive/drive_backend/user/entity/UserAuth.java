package com.gumraze.drive.drive_backend.user.entity;

import com.gumraze.drive.drive_backend.auth.constants.AuthProvider;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_auth")
public class UserAuth {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // User 1 <- N UserAuth(Kakao, Google, ... )
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private AuthProvider provider;
    private String providerUserId;

    private String email;
    private String nickname;
    private String profileImageUrl;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
