package com.gumraze.drive.drive_backend.user.entity;

import com.gumraze.drive.drive_backend.auth.constants.AuthProvider;
import com.gumraze.drive.drive_backend.user.constants.Gender;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Column(name = "thumbnail_image_url")
    private String thumbnailImageUrl;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "age_range")
    private String ageRange;

    @Column(name = "birthday")
    private String birthday;

    @Column(name = "is_email_verified")
    private Boolean isEmailVerified;

    @Column(name = "is_phone_number_verified")
    private Boolean isPhoneNumberVerified;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}