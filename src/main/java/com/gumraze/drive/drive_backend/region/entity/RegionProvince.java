package com.gumraze.drive.drive_backend.region.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "region_province")
public class RegionProvince {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;        // 지역 기본 키

    @Column(nullable = false, length = 50)
    private String name;    // 표시용 지역명

    @Column(nullable = false, length = 20, unique = true)
    private String code;    // 행정구역/법정동 코드

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
