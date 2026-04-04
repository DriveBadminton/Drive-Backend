package com.gumraze.rallyon.backend.courtManager.application.port.in.result;

import java.util.List;

/**
 * AI 코트 배정 프리뷰 유스케이스의 application 결과다.
 *
 * @param rounds AI가 계산한 라운드별 코트 슬롯 배정 결과
 * @param warnings 프리뷰 생성은 성공했지만 일부 조건을 완전히 만족하지 못한 경우의 경고 목록
 */
public record CreateFreeGameAssignmentPreviewResult(
        List<Round> rounds,
        List<Warning> warnings
) {
    /**
     * 한 라운드의 추천 배정 결과다.
     *
     * @param roundNumber 라운드 순번
     * @param courts 해당 라운드에 대한 코트별 슬롯 결과
     */
    public record Round(
            Integer roundNumber,
            List<Court> courts
    ) {
    }

    /**
     * 한 코트의 추천 슬롯 결과다.
     *
     * @param courtNumber 코트 순번
     * @param slots 각 슬롯에 배정된 participant clientId 목록
     */
    public record Court(
            Integer courtNumber,
            List<String> slots
    ) {
    }

    /**
     * 프리뷰 생성은 성공했지만 일부 제약을 만족시키지 못한 경우의 경고다.
     *
     * @param code 프론트 분기 처리용 경고 코드
     * @param message 사용자 또는 운영자에게 보여줄 경고 메시지
     */
    public record Warning(
            String code,
            String message
    ) {}
}
