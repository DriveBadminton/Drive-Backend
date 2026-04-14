package com.gumraze.rallyon.backend.courtManager.application.port.in;

import com.gumraze.rallyon.backend.courtManager.application.port.in.command.CreateFreeGameAssignmentPreviewCommand;
import com.gumraze.rallyon.backend.courtManager.application.port.in.result.SubmitFreeGameAssignmentPreviewJobResult;

import java.util.UUID;

/**
 * AI 자동 배정 preview job 제출 유스케이스다.
 */
public interface SubmitFreeGameAssignmentPreviewUseCase {

    /**
     * 현재 화면 상태를 기반으로 preview job을 제출한다.
     *
     * @param accountId 요청자 계정 ID
     * @param command AI preview 입력 command
     * @return 제출된 preview job 영수증
     */
    SubmitFreeGameAssignmentPreviewJobResult submit(
            UUID accountId,
            CreateFreeGameAssignmentPreviewCommand command
    );
}
