package com.gumraze.rallyon.backend.courtManager.adapter.out.ai;

import java.util.List;

/**
 * AI가 반환하는 코트 배정 프리뷰 구조다.
 *
 * <p>Spring AI의 structured output을 이 타입으로 역직렬화한 뒤,
 * adapter가 application result로 변환한다.
 *
 * @param rounds AI가 추천한 라운드별 코트 배정 결과
 * @param warnings 프리뷰 생성은 성공했지만 일부 제약을 완전히 만족하지 못한 경우의 경고 목록
 */
public record AssignmentPreviewAiResponse(
        List<Round> rounds,
        List<Warning> warnings
) {

    /**
     * AI가 반환한 한 라운드의 배정 결과다.
     *
     * @param roundNumber 라운드 순번
     * @param courts 해당 라운드의 코트별 슬롯 결과
     */
    public record Round(
            Integer roundNumber,
            List<Court> courts
    ) {
    }

    /**
     * AI가 반환한 한 코트의 슬롯 결과다.
     *
     * @param courtNumber 코트 순번
     * @param slots 각 슬롯에 배정된 participantId 목록
     */
    public record Court(
            Integer courtNumber,
            List<Long> slots
    ) {
    }

    /**
     * AI가 반환한 경고 정보다.
     *
     * @param code 프론트 분기 처리용 경고 코드
     * @param message 사용자에게 표시할 경고 메시지
     */
    public record Warning(
            String code,
            String message
    ) {
    }
}
