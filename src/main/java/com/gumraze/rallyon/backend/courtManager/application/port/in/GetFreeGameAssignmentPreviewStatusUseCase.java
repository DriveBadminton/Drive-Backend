package com.gumraze.rallyon.backend.courtManager.application.port.in;

import com.gumraze.rallyon.backend.courtManager.application.port.in.result.GetFreeGameAssignmentPreviewJobStatusResult;

import java.util.UUID;

/**
 * AI 자동 배정 preview job 상태 조회 유스케이스다.
 */
public interface GetFreeGameAssignmentPreviewStatusUseCase {

    /**
     * preview job의 현재 상태를 조회한다.
     *
     * @param accountId 요청자 계정 ID
     * @param jobId 조회할 preview job ID
     * @return preview job 상태와 완료 결과
     */
    GetFreeGameAssignmentPreviewJobStatusResult getStatus(UUID accountId, UUID jobId);
}
