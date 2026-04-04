package com.gumraze.rallyon.backend.courtManager.dto;

import java.util.List;

/**
 * AI 코트 배정 프리뷰 생성 결과를 반환한다.
 *
 * <p>이 응답은 자유게임을 최종 저장한 결과가 아니라,
 * 현재 참가자/코트/정책 기준으로 AI가 제안한 배정 초안이다.
 *
 * <p>프론트는 이 응답을 코트 배정 화면에 반영한 뒤,
 * 사용자가 결과를 검토하거나 수동 수정한 다음
 * 별도의 자유게임 생성 API를 호출한다.
 */
public record CreateFreeGameAssignmentPreviewResponse(
        List<RoundResponse> rounds,
        List<WarningResponse> warnings
) {
    /**
     * 한 라운드에 대한 AI 배정 결과다.
     *
     * <p>request의 round 구조와 같은 축을 유지하므로,
     * 프론트는 roundNumber를 기준으로 현재 화면의 라운드와 대응시켜
     * 각 코트 슬롯을 덮어쓸 수 있다.
     */
    public record RoundResponse(
            Integer roundNumber,
            List<CourtResponse> courts
    ) {
    }

    /**
     * 한 코트의 슬롯 배정 결과다.
     *
     * <p>slots는 항상 4칸을 가지며,
     * 각 값은 request에 들어온 participant의 clientId를 그대로 사용한다.
     *
     * <p>즉 이 응답은 참가자 전체 정보를 다시 반환하지 않고,
     * "어떤 참가자가 어느 슬롯에 들어가야 하는지"만 알려준다.
     */
    public record CourtResponse(
            Integer courtNumber,
            List<String> slots
    ) {
    }

    /**
     * AI 배정 과정에서 발생한 제약 또는 주의사항이다.
     *
     * <p>프리뷰 생성은 성공했지만,
     * 일부 선호 조건을 완전히 만족하지 못한 경우를 표현한다.
     *
     * <p>예를 들어:
     * <p>- 파트너 우선 정책을 모두 반영하지 못한 경우
     * <p>- 기존 배정 유지 때문에 최적 배치가 제한된 경우
     * <p>- 참가자 수나 슬롯 수 제약 때문에 일부 조정이 필요한 경우
     *
     * <p>code는 프론트 분기 처리용 식별자,
     * message는 사용자 또는 운영자에게 보여줄 설명 문구다.
     */
    public record WarningResponse(
            String code,
            String message
    ) {
    }
}
