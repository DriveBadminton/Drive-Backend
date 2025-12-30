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

    /**
     * Create a new UserAuth that links a User to an external authentication provider.
     *
     * Initializes the creation and update timestamps to the current time.
     *
     * @param user the owning User entity
     * @param provider the authentication provider
     * @param providerUserId the identifier of the user issued by the authentication provider
     */
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

    /**
     * Update this UserAuth with contact and profile attributes from the given OAuth user information.
     *
     * <p>The method replaces email, nickname, profile and thumbnail image URLs, gender, age range,
     * birthday, email verification flag, and phone number verification flag with values from
     * {@code userInfo}, and sets {@code updatedAt} to the current time.</p>
     *
     * @param userInfo source of OAuth-provided user attributes used to refresh this authentication record
     */
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