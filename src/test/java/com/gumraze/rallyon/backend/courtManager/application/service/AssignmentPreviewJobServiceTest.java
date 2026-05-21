package com.gumraze.rallyon.backend.courtManager.application.service;

import com.gumraze.rallyon.backend.common.exception.ConflictException;
import com.gumraze.rallyon.backend.courtManager.adapter.out.ai.AssignmentPreviewAiProperties;
import com.gumraze.rallyon.backend.courtManager.application.port.in.command.CreateFreeGameAssignmentPreviewCommand;
import com.gumraze.rallyon.backend.courtManager.application.port.in.result.GetFreeGameAssignmentPreviewJobStatusResult;
import com.gumraze.rallyon.backend.courtManager.application.port.in.result.SubmitFreeGameAssignmentPreviewJobResult;
import com.gumraze.rallyon.backend.courtManager.application.port.out.ManageAssignmentPreviewJobPort;
import com.gumraze.rallyon.backend.courtManager.constants.AssignmentPreviewJobFailureCode;
import com.gumraze.rallyon.backend.courtManager.constants.AssignmentPreviewJobStatus;
import com.gumraze.rallyon.backend.courtManager.entity.AssignmentPreviewJob;
import com.gumraze.rallyon.backend.user.constants.Gender;
import com.gumraze.rallyon.backend.user.constants.Grade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.core.task.TaskRejectedException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
class AssignmentPreviewJobServiceTest {

    @Mock
    private ManageAssignmentPreviewJobPort manageAssignmentPreviewJobPort;

    @Mock
    private ProcessFreeGameAssignmentPreviewJobService processFreeGameAssignmentPreviewJobService;

    @Mock
    private AssignmentPreviewJobDispatchFailureService assignmentPreviewJobDispatchFailureService;

    @Mock
    private AssignmentPreviewAiProperties assignmentPreviewAiProperties;

    @InjectMocks
    private AssignmentPreviewJobService assignmentPreviewJobService;

    @Test
    @DisplayName("진행 중인 preview job이 없으면 새 job을 저장하고 worker를 실행한다")
    void submit_whenNoActiveJobExists_savesJobAndDispatchesWorker() {
        UUID accountId = UUID.randomUUID();
        CreateFreeGameAssignmentPreviewCommand command = previewCommand();
        AssignmentPreviewJob queuedJob = AssignmentPreviewJob.queue(accountId, command, "gpt-5-mini");

        given(assignmentPreviewAiProperties.getModel()).willReturn("gpt-5-mini");
        given(manageAssignmentPreviewJobPort.existsByRequesterAccountIdAndStatusIn(
                eq(accountId),
                any(List.class)
        )).willReturn(false);
        given(manageAssignmentPreviewJobPort.save(any(AssignmentPreviewJob.class))).willReturn(queuedJob);

        SubmitFreeGameAssignmentPreviewJobResult result =
                assignmentPreviewJobService.submit(accountId, command);

        then(result.jobId()).isEqualTo(queuedJob.getId());
        then(result.status()).isEqualTo(AssignmentPreviewJobStatus.QUEUED);
        then(result.pollAfterMs()).isEqualTo(1000);
        verify(processFreeGameAssignmentPreviewJobService).processAsync(queuedJob.getId());
    }

    @Test
    @DisplayName("worker 제출이 reject되면 preview job을 즉시 실패 처리한다")
    void submit_whenDispatchRejected_marksQueuedJobFailed(CapturedOutput output) {
        UUID accountId = UUID.randomUUID();
        CreateFreeGameAssignmentPreviewCommand command = previewCommand();
        AssignmentPreviewJob queuedJob = AssignmentPreviewJob.queue(accountId, command, "gpt-5-mini");
        LocalDateTime completedAt = LocalDateTime.now();
        queuedJob.markFailed(
                AssignmentPreviewJobFailureCode.SERVICE_UNAVAILABLE,
                "자동 배정 작업을 시작하지 못했어요. 잠시 후 다시 시도해주세요.",
                "gpt-5-mini",
                false,
                null,
                null,
                completedAt,
                0L,
                25L
        );

        given(assignmentPreviewAiProperties.getModel()).willReturn("gpt-5-mini");
        given(manageAssignmentPreviewJobPort.existsByRequesterAccountIdAndStatusIn(
                eq(accountId),
                any(List.class)
        )).willReturn(false);
        given(manageAssignmentPreviewJobPort.save(any(AssignmentPreviewJob.class))).willReturn(queuedJob);
        given(manageAssignmentPreviewJobPort.findById(queuedJob.getId())).willReturn(Optional.of(queuedJob));
        doThrow(new TaskRejectedException("executor saturated"))
                .when(processFreeGameAssignmentPreviewJobService)
                .processAsync(queuedJob.getId());

        assignmentPreviewJobService.submit(accountId, command);

        verify(assignmentPreviewJobDispatchFailureService).markQueuedJobAsDispatchFailed(queuedJob.getId());
        then(output.getOut()).contains("[ASSIGNMENT_PREVIEW_JOB] event=DISPATCH_FAILED");
    }

    @Test
    @DisplayName("진행 중인 preview job이 있으면 충돌 예외를 던진다")
    void submit_whenActiveJobExists_throwsConflictException() {
        UUID accountId = UUID.randomUUID();

        given(manageAssignmentPreviewJobPort.existsByRequesterAccountIdAndStatusIn(
                eq(accountId),
                any(List.class)
        )).willReturn(true);

        assertThatThrownBy(() -> assignmentPreviewJobService.submit(accountId, previewCommand()))
                .isInstanceOf(ConflictException.class)
                .hasMessage("이미 자동 배정이 진행 중이에요. 잠시만 기다려주세요.");
    }

    @Test
    @DisplayName("owner 기준으로 preview job 상태를 반환한다")
    void getStatus_whenOwnedJobExists_returnsStatusResult() {
        UUID accountId = UUID.randomUUID();
        AssignmentPreviewJob job = AssignmentPreviewJob.queue(accountId, previewCommand(), "gpt-5-mini");
        LocalDateTime completedAt = LocalDateTime.now();
        job.markSucceeded(
                previewResult(),
                "gpt-5-mini",
                true,
                1200L,
                700L,
                completedAt,
                1900L,
                2100L
        );

        given(manageAssignmentPreviewJobPort.findByIdAndRequesterAccountId(job.getId(), accountId))
                .willReturn(Optional.of(job));

        GetFreeGameAssignmentPreviewJobStatusResult result =
                assignmentPreviewJobService.getStatus(accountId, job.getId());

        then(result.jobId()).isEqualTo(job.getId());
        then(result.status()).isEqualTo(AssignmentPreviewJobStatus.SUCCEEDED);
        then(result.preview()).isNotNull();
        then(result.preview().rounds().getFirst().courts().getFirst().slots())
                .containsExactly(1L, 2L, null, null);
        then(result.failure()).isNull();
    }

    @Test
    @DisplayName("앱 시작 시 진행 중이던 preview job을 실패 처리한다")
    void recoverInterruptedJobs_marksActiveJobsAsFailed() {
        UUID accountId = UUID.randomUUID();
        AssignmentPreviewJob queuedJob = AssignmentPreviewJob.queue(accountId, previewCommand(), "gpt-5-mini");
        AssignmentPreviewJob runningJob = AssignmentPreviewJob.queue(accountId, previewCommand(), "gpt-5-mini");
        runningJob.markRunning(LocalDateTime.now().minusSeconds(3), 150L);

        given(manageAssignmentPreviewJobPort.findAllByStatusIn(any(List.class)))
                .willReturn(List.of(queuedJob, runningJob));
        given(manageAssignmentPreviewJobPort.save(any(AssignmentPreviewJob.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        int recovered = assignmentPreviewJobService.recoverInterruptedJobs();

        then(recovered).isEqualTo(2);
        then(queuedJob.getStatus()).isEqualTo(AssignmentPreviewJobStatus.FAILED);
        then(queuedJob.getFailureCode()).isEqualTo(AssignmentPreviewJobFailureCode.WORKER_RESTARTED);
        then(runningJob.getStatus()).isEqualTo(AssignmentPreviewJobStatus.FAILED);
        verify(manageAssignmentPreviewJobPort, times(2)).save(any(AssignmentPreviewJob.class));
    }

    private CreateFreeGameAssignmentPreviewCommand previewCommand() {
        return new CreateFreeGameAssignmentPreviewCommand(
                List.of(
                        new CreateFreeGameAssignmentPreviewCommand.Participant(
                                1L,
                                "서승재",
                                Gender.MALE,
                                20,
                                Grade.S,
                                1
                        ),
                        new CreateFreeGameAssignmentPreviewCommand.Participant(
                                2L,
                                "김원호",
                                Gender.MALE,
                                20,
                                Grade.S,
                                1
                        )
                ),
                List.of(
                        new CreateFreeGameAssignmentPreviewCommand.Round(
                                1,
                                List.of(
                                        new CreateFreeGameAssignmentPreviewCommand.Court(
                                                1,
                                                Arrays.asList(1L, null, null, null)
                                        )
                                )
                        )
                ),
                List.of(
                        new CreateFreeGameAssignmentPreviewCommand.PartnerPairs(1L, 2L)
                ),
                new CreateFreeGameAssignmentPreviewCommand.Preferences(
                        CreateFreeGameAssignmentPreviewCommand.PartnerPolicy.PREFER_PARTNERS,
                        CreateFreeGameAssignmentPreviewCommand.ExistingAssignmentPolicy.FILL_EMPTY_SLOTS
                )
        );
    }

    private com.gumraze.rallyon.backend.courtManager.application.port.in.result.CreateFreeGameAssignmentPreviewResult previewResult() {
        return new com.gumraze.rallyon.backend.courtManager.application.port.in.result.CreateFreeGameAssignmentPreviewResult(
                List.of(
                        new com.gumraze.rallyon.backend.courtManager.application.port.in.result.CreateFreeGameAssignmentPreviewResult.Round(
                                1,
                                List.of(
                                        new com.gumraze.rallyon.backend.courtManager.application.port.in.result.CreateFreeGameAssignmentPreviewResult.Court(
                                                1,
                                                Arrays.asList(1L, 2L, null, null)
                                        )
                                )
                        )
                ),
                List.of()
        );
    }
}
