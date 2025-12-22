package com.gumraze.drive.drive_backend.region.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "region")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Region {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;        // 지역 기본 키

    private Long parentId;  // 상위 지역 ID

    private Integer level;  // 지역 깊이( 1 -> 시/도, 2 -> 시/군/구 )
    private String name;    // 표시용 지역명
    private String code;    // 행정구역/법정동 코드

    private LocalDateTime createdAt;
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
