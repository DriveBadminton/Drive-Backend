package com.gumraze.drive.drive_backend.courtmanager.entity;

import java.time.LocalDateTime;

import com.gumraze.drive.drive_backend.courtmanager.constants.ParticipantGender;
import com.gumraze.drive.drive_backend.courtmanager.constants.ParticipantGrade;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
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
@Table(name = "court_tournament_participants")
public class CourtTournamentParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    private CourtTournament tournament;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ParticipantGender gender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ParticipantGrade grade;

    @Column(name = "participant_name", nullable = false, length = 150)
    private String participantName;

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

    void setTournament(CourtTournament tournament) {
        this.tournament = tournament;
    }
}
