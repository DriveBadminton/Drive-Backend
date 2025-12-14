package com.gumraze.drive.drive_backend.user.entity;

import com.gumraze.drive.drive_backend.user.constants.Grade;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_grade_history")
public class UserGradeHistory {
    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private Grade grade;

    private LocalDateTime changed_at;
}
