package com.gumraze.rallyon.backend.courtManager.application.port.out;

import com.gumraze.rallyon.backend.courtManager.application.port.in.command.CreateFreeGameAssignmentPreviewCommand;
import com.gumraze.rallyon.backend.courtManager.application.port.out.result.FreeGameAssignmentPreviewGeneration;

/**
 * AI 자동 배정 preview 생성 실행 메타데이터를 반환하는 outbound port다.
 */
public interface GenerateFreeGameAssignmentPreviewExecutionPort {

    /**
     * AI 자동 배정 preview를 생성하고 실행 메타데이터를 함께 반환한다.
     *
     * @param command 현재 화면 기준 preview 입력
     * @return preview 결과와 모델/재시도/elapsed 정보
     */
    FreeGameAssignmentPreviewGeneration generateExecution(CreateFreeGameAssignmentPreviewCommand command);
}
