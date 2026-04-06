package com.gumraze.rallyon.backend.courtManager.adapter.out.ai;

import com.gumraze.rallyon.backend.courtManager.application.port.in.command.CreateFreeGameAssignmentPreviewCommand;

/**
 * AI 코트 배정 프리뷰 생성을 요청하는 클라이언트다.
 *
 * <p>adapter는 이 인터페이스를 통해 AI 모델 호출을 위임하고,
 * 호출 결과를 structured output으로 전달받는다.
 */
public interface AssignmentPreviewAiClient {

    /**
     * AI 코트 배정 프리뷰를 생성한다.
     *
     * @param command 현재 참가자, 코트 슬롯, 파트너 관계, 배정 선호 정책
     * @return AI가 반환한 코트 배정 프리뷰 구조
     */
    AssignmentPreviewAiResponse generate(CreateFreeGameAssignmentPreviewCommand command);
}
