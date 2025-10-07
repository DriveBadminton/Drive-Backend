package com.gumraze.drive.drive_backend.courtmanager.dto;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "코트 매니저 대회 생성 응답")
public record CourtTournamentResponse(
    @Schema(description = "대회 ID", example = "1")
    Long id,
    @Schema(description = "대회 이름", example = "2025 가을 리그")
    String name,
    @Schema(description = "공개 여부", example = "PUBLIC")
    String visibility,
    @Schema(description = "주최자 OAuth 사용자 ID", example = "10")
    Long organizerId,
    @Schema(description = "생성 일시")
    LocalDateTime createdAt,
    @Schema(description = "참가자 목록")
    List<Participant> participants
) {
    @Schema(description = "참가자 응답")
    public record Participant(
        @Schema(description = "참가자 ID", example = "5")
        Long id,
        @Schema(description = "참가자 이름", example = "홍길동")
        String name,
        @Schema(description = "참가자 성별", example = "남성")
        String gender,
        @Schema(description = "참가자 급수", example = "B급")
        String grade
    ) {
    }
}
