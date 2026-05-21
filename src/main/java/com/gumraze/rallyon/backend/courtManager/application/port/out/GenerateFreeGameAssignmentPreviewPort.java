package com.gumraze.rallyon.backend.courtManager.application.port.out;

import com.gumraze.rallyon.backend.courtManager.application.port.in.command.CreateFreeGameAssignmentPreviewCommand;
import com.gumraze.rallyon.backend.courtManager.application.port.in.result.CreateFreeGameAssignmentPreviewResult;

/**
 * AI 코트 배정 프리뷰 생성을 외부 시스템에 위임하는 outbound port다.
 *
 * <p>application 계층은 이 port를 통해 프리뷰 생성을 요청하고,
 * 실제 LLM 호출이나 외부 연동 방식은 adapter가 구현한다.
 */
public interface GenerateFreeGameAssignmentPreviewPort {

    /**
     * 입력된 배정 조건을 기준으로 AI 프리뷰를 생성한다.
     *
     * @param command 현재 참가자, 코트 슬롯, 파트너 관계, 배정 선호 정책
     * @return 라운드별 추천 배정 결과와 경고 목록
     */
    CreateFreeGameAssignmentPreviewResult generate(CreateFreeGameAssignmentPreviewCommand command);
}
