package com.gumraze.rallyon.backend.courtManager.application.port.in;

import com.gumraze.rallyon.backend.courtManager.application.port.in.command.CreateFreeGameAssignmentPreviewCommand;
import com.gumraze.rallyon.backend.courtManager.application.port.in.result.CreateFreeGameAssignmentPreviewResult;

/**
 * 자유게임 코트 배정 프리뷰 생성을 위한 유스케이스다.
 *
 * <p>현재 참가자, 코트 슬롯 상태, 파트너 관계, 사용자 선택 정책을 입력으로 받아
 * AI가 제안한 배정 초안을 반환한다.
 */
public interface CreateFreeGameAssignmentPreviewUseCase {

    /**
     * AI 코트 배정 프리뷰를 생성한다.
     *
     * @param command 현재 화면 상태와 배정 선호 정책
     * @return 라운드별 추천 배정 결과와 경고 목록
     */
    CreateFreeGameAssignmentPreviewResult create(CreateFreeGameAssignmentPreviewCommand command);
}
