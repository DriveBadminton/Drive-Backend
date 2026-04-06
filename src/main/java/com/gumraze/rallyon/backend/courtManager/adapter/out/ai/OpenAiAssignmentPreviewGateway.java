package com.gumraze.rallyon.backend.courtManager.adapter.out.ai;

import com.gumraze.rallyon.backend.courtManager.application.port.in.command.CreateFreeGameAssignmentPreviewCommand;

/**
 * OpenAI Responses API 호출을 위임하는 경계다.
 *
 * <p>이 인터페이스는 assignment preview 생성 요청을 OpenAI에 전달하고,
 * structured output을 AI 프리뷰 구조로 반환한다.
 */
public interface OpenAiAssignmentPreviewGateway {

    /**
     * OpenAI를 통해 코트 배정 프리뷰를 생성한다.
     *
     * @param command 현재 참가자, 코트 슬롯, 파트너 관계, 배정 선호 정책
     * @return OpenAI가 반환한 AI 프리뷰 구조
     */
    AssignmentPreviewAiResponse generate(CreateFreeGameAssignmentPreviewCommand command);
}
