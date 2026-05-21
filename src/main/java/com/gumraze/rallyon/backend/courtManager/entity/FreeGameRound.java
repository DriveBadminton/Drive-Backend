package com.gumraze.rallyon.backend.courtManager.entity;

import com.gumraze.rallyon.backend.common.persistence.MutableAuditEntity;
import com.gumraze.rallyon.backend.courtManager.constants.RoundStatus;
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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "free_game_round",
        uniqueConstraints = @UniqueConstraint(columnNames = {"freegame_id", "round_number"})
)
public class FreeGameRound extends MutableAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "freegame_id", nullable = false)
    private FreeGame freeGame;

    @Column(name = "round_number", nullable = false)
    private Integer roundNumber;        // 라운드의 순서

    @Enumerated(EnumType.STRING)
    private RoundStatus roundStatus;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected FreeGameRound() {
    }

    public static FreeGameRound create(
            FreeGame freeGame,
            Integer roundNumber,
            RoundStatus roundStatus
    ) {
        FreeGameRound round = new FreeGameRound();
        round.freeGame = freeGame;
        round.roundNumber = roundNumber;
        round.roundStatus = roundStatus;
        return round;
    }

    public UUID getId() {
        return id;
    }

    public FreeGame getFreeGame() {
        return freeGame;
    }

    public Integer getRoundNumber() {
        return roundNumber;
    }

    public RoundStatus getRoundStatus() {
        return roundStatus;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public void start() {
        if (roundStatus == RoundStatus.COMPLETED) {
            throw new IllegalStateException("완료된 라운드는 다시 시작할 수 없습니다.");
        }
        roundStatus = RoundStatus.IN_PROGRESS;
    }

    public void finish(LocalDateTime finishedAt) {
        this.roundStatus = RoundStatus.COMPLETED;
        this.finishedAt = finishedAt;
    }

    @Override
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    protected void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    protected void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
