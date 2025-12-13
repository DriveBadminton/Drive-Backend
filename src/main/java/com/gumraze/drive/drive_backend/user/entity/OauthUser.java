package com.gumraze.drive.drive_backend.user.entity;

import com.gumraze.drive.drive_backend.user.constants.AuthProvider;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "oauth_users", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"oauth_provider", "oauth_id"})
})
public class OauthUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "oauth_provider", nullable = false, length = 20)
    private AuthProvider oauthProvider;

    @Column(name = "oauth_id", nullable = false, length = 100)
    private String oauthId;

    @Column(length = 320)
    private String email;

    @Column(length = 100)
    private String nickname;

    @Column(length = 500)
    private String profileImageUrl;

    public OauthUser updateProfile(String email, String nickname, String profileImageUrl) {
        this.email = email;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        return this;
    }
}
