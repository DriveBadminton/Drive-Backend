package com.gumraze.drive.drive_backend.user.entity;

import com.gumraze.drive.drive_backend.region.entity.RegionDistrict;
import com.gumraze.drive.drive_backend.user.constants.Gender;
import com.gumraze.drive.drive_backend.user.constants.Grade;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_profile")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProfile {
    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id")
    private User user;

    private String nickname;
    private String profileImageUrl;

    private LocalDateTime birth;
    private boolean birthVisible;

    // 지역급수, 전국급수
    @Enumerated(EnumType.STRING)
    private Grade regionalGrade;
    @Enumerated(EnumType.STRING)
    private Grade nationalGrade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id")
    private RegionDistrict regionDistrict;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(length = 4, nullable = false)
    private String tag;

    @Column(name = "tag_changed_at", nullable = false)
    private LocalDateTime tagChangedAt;

    private LocalDateTime createdAt;    // 계정 생성 시점이 아닌 프로필 생성 시점
    private LocalDateTime updatedAt;

    /**
     * Create a UserProfile with the given identity and profile attributes.
     *
     * Initializes identifier, display name, regional and national grades, and region district.
     * Also sets `createdAt` and `updatedAt` to the current time.
     *
     * @param id             the primary key for the profile (may be null for transient instances)
     * @param nickname       the display name for the user
     * @param regionalGrade  the regional grade enum value
     * @param nationalGrade  the national grade enum value
     * @param regionDistrict the region district associated with the profile
     */
    public UserProfile(
            Long id,
            String nickname,
            Grade regionalGrade,
            Grade nationalGrade,
            RegionDistrict regionDistrict
    ) {
        this.id = id;
        this.nickname = nickname;
        this.regionalGrade = regionalGrade;
        this.nationalGrade = nationalGrade;
        this.regionDistrict = regionDistrict;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Update profile fields and refresh the profile's last-updated timestamp.
     *
     * @param nickname       the new display name for the profile
     * @param regionalGrade  the new regional grade to assign to the profile
     * @param nationalGrade  the new national grade to assign to the profile
     * @param regionDistrict the new region district to associate with the profile
     */
    public void updateProfile(
            String nickname,
            Grade regionalGrade,
            Grade nationalGrade,
            RegionDistrict regionDistrict
    ) {
        this.nickname = nickname;
        this.regionalGrade = regionalGrade;
        this.nationalGrade = nationalGrade;
        this.regionDistrict = regionDistrict;
        this.updatedAt = LocalDateTime.now();
    }
}