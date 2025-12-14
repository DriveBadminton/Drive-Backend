package com.gumraze.drive.drive_backend.user.entity;

import com.gumraze.drive.drive_backend.user.constants.Grade;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_profile")
public class UserProfile {
    @Id
    private Long id;

    private String nickname;
    private String profileImageUrl;

    private LocalDateTime birthDate;
    private boolean birthVisible;

    // JPA에서 enum 타입 필드를 DB에 어떻게 저장할지 저장하는 어노테이션
    @Enumerated(EnumType.STRING)
    private Grade grade;

    private LocalDateTime createdAt;    // 계정 생성 시점이 아닌 프로필 생성 시점
    private LocalDateTime updatedAt;
}
