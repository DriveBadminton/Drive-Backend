package com.gumraze.rallyon.backend.courtManager.application.port.out;

import com.gumraze.rallyon.backend.courtManager.constants.AssignmentPreviewJobStatus;
import com.gumraze.rallyon.backend.courtManager.entity.AssignmentPreviewJob;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * AI 자동 배정 preview job persistence를 관리하는 outbound port다.
 */
public interface ManageAssignmentPreviewJobPort {

    /**
     * 요청 계정에 진행 중인 preview job이 있는지 확인한다.
     *
     * @param requesterAccountId 작업 요청자 계정 ID
     * @param statuses active로 간주할 상태 목록
     * @return 진행 중인 job 존재 여부
     */
    boolean existsByRequesterAccountIdAndStatusIn(
            UUID requesterAccountId,
            Collection<AssignmentPreviewJobStatus> statuses
    );

    /**
     * preview job을 저장하고 즉시 flush한다.
     *
     * @param job 저장 대상 job
     * @return 저장된 job
     */
    AssignmentPreviewJob save(AssignmentPreviewJob job);

    /**
     * job ID로 preview job을 조회한다.
     *
     * @param jobId preview job ID
     * @return 저장된 job
     */
    Optional<AssignmentPreviewJob> findById(UUID jobId);

    /**
     * owner 검증과 함께 preview job을 조회한다.
     *
     * @param jobId preview job ID
     * @param requesterAccountId 요청자 계정 ID
     * @return 저장된 job
     */
    Optional<AssignmentPreviewJob> findByIdAndRequesterAccountId(UUID jobId, UUID requesterAccountId);

    /**
     * 특정 상태 목록에 해당하는 preview job을 모두 조회한다.
     *
     * @param statuses 조회할 상태 목록
     * @return 상태가 일치하는 job 목록
     */
    List<AssignmentPreviewJob> findAllByStatusIn(Collection<AssignmentPreviewJobStatus> statuses);
}
