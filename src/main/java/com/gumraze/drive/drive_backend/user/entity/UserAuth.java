package com.gumraze.drive.drive_backend.user.entity;

import com.gumraze.drive.drive_backend.auth.constants.AuthProvider;
import com.gumraze.drive.drive_backend.auth.oauth.OAuthUserInfo;
import com.gumraze.drive.drive_backend.user.constants.Gender;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
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

    // 최소 필수값만 받는 빌더로 지정
    @Builder
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

    public void updateFromOAuth(OAuthUserInfo userInfo) {
        this.email = userInfo.email();
        this.nickname = userInfo.nickname();
        this.profileImageUrl = userInfo.profileImageUrl();
        this.thumbnailImageUrl = userInfo.thumbnailImageUrl();
        this.gender = userInfo.gender();
        this.ageRange = userInfo.ageRange();
        this.birthday = userInfo.birthday();
        this.isEmailVerified = userInfo.emailVerified();
        this.isPhoneNumberVerified = userInfo.phoneNumberVerified();
        this.updatedAt = LocalDateTime.now();
    }
}
