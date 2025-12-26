package com.gumraze.drive.drive_backend.user.entity;

import com.gumraze.drive.drive_backend.region.entity.RegionDistrict;
import com.gumraze.drive.drive_backend.user.constants.Gender;
import com.gumraze.drive.drive_backend.user.constants.Grade;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_profile")
@Getter
@Setter
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

    // JPA에서 enum 타입 필드를 DB에 어떻게 저장할지 저장하는 어노테이션
    @Enumerated(EnumType.STRING)
    private Grade grade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id")
    private RegionDistrict regionDistrict;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private LocalDateTime createdAt;    // 계정 생성 시점이 아닌 프로필 생성 시점
    private LocalDateTime updatedAt;

    public UserProfile(
            Long id,
            String nickname,
            Grade grade,
            RegionDistrict regionDistrict
    ) {
        this.id = id;
        this.nickname = nickname;
        this.grade = grade;
        this.regionDistrict = regionDistrict;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void updateProfile(
            String nickname,
            Grade grade,
            RegionDistrict regionDistrict
    ) {
        this.nickname = nickname;
        this.grade = grade;
        this.regionDistrict = regionDistrict;
        this.updatedAt = LocalDateTime.now();
    }
}
