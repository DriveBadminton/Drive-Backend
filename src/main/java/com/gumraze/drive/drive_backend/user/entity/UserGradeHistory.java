package com.gumraze.drive.drive_backend.user.entity;

import com.gumraze.drive.drive_backend.user.constants.Grade;
import com.gumraze.drive.drive_backend.user.constants.GradeType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_grade_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserGradeHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private Grade grade;

    @Enumerated(EnumType.STRING)
    private GradeType gradeType;

    @Column(name = "changed_at")
    private LocalDateTime changedAt;

    public UserGradeHistory(
            User user,
            Grade grade,
            GradeType gradeType
    ) {
        this.user = user;
        this.grade = grade;
        this.gradeType = gradeType;
        this.changedAt = LocalDateTime.now();
    }
}
