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
    @Column(name = "id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id")
    @MapsId
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

}