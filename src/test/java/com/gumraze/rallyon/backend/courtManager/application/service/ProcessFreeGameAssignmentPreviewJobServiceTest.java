package com.gumraze.rallyon.backend.courtManager.application.service;

import com.gumraze.rallyon.backend.courtManager.adapter.out.ai.AssignmentPreviewAiInvalidResponseException;
import com.gumraze.rallyon.backend.courtManager.application.port.in.command.CreateFreeGameAssignmentPreviewCommand;
import com.gumraze.rallyon.backend.courtManager.application.port.in.result.CreateFreeGameAssignmentPreviewResult;
import com.gumraze.rallyon.backend.courtManager.application.port.out.GenerateFreeGameAssignmentPreviewExecutionPort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.ManageAssignmentPreviewJobPort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.result.FreeGameAssignmentPreviewGeneration;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;

@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
class ProcessFreeGameAssignmentPreviewJobServiceTest {

    @Mock
    private ManageAssignmentPreviewJobPort manageAssignmentPreviewJobPort;

    @Mock
    private GenerateFreeGameAssignmentPreviewExecutionPort generateFreeGameAssignmentPreviewExecutionPort;

    @InjectMocks
    private ProcessFreeGameAssignmentPreviewJobService processFreeGameAssignmentPreviewJobService;

    @Test
    @DisplayName("AI preview 생성에 성공하면 job을 성공 상태로 저장한다")
    void process_whenGenerationSucceeds_marksJobSucceeded() {
        AssignmentPreviewJob job = AssignmentPreviewJob.queue(UUID.randomUUID(), previewCommand(), "gpt-5-mini");
        FreeGameAssignmentPreviewGeneration generation =
                new FreeGameAssignmentPreviewGeneration(
                        previewResult(),
                        "gpt-5-mini",
                        true,
                        1100L,
                        600L,
                        true,
                        180L,
                        2,
                        220L,
                        List.of("UNDER_FILLED"),
                        4,
                        2,
                        4,
                        List.of(),
                        300,
                        580,
                        170,
                        1200
                );

        given(manageAssignmentPreviewJobPort.findById(job.getId())).willReturn(Optional.of(job));
        given(manageAssignmentPreviewJobPort.save(any(AssignmentPreviewJob.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        given(generateFreeGameAssignmentPreviewExecutionPort.generateExecution(job.getRequestCommand()))
                .willReturn(generation);

        processFreeGameAssignmentPreviewJobService.process(job.getId());

        then(job.getStatus()).isEqualTo(AssignmentPreviewJobStatus.SUCCEEDED);
        then(job.getResultPreview()).isEqualTo(previewResult());
        then(job.isRepairAttempted()).isTrue();
        then(job.getInitialAiElapsedMs()).isEqualTo(1100L);
        then(job.getRepairAiElapsedMs()).isEqualTo(600L);
        verify(manageAssignmentPreviewJobPort, atLeast(2)).save(any(AssignmentPreviewJob.class));
    }

    @Test
    @DisplayName("AI preview 생성이 invalid output으로 실패하면 job을 실패 상태로 저장한다")
    void process_whenGenerationFails_marksJobFailed() {
        AssignmentPreviewJob job = AssignmentPreviewJob.queue(UUID.randomUUID(), previewCommand(), "gpt-5-mini");

        given(manageAssignmentPreviewJobPort.findById(job.getId())).willReturn(Optional.of(job));
        given(manageAssignmentPreviewJobPort.save(any(AssignmentPreviewJob.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        given(generateFreeGameAssignmentPreviewExecutionPort.generateExecution(job.getRequestCommand()))
                .willThrow(new AssignmentPreviewAiInvalidResponseException(
                        "OpenAI 응답 구조가 요청과 일치하지 않습니다.",
                        null,
                        "gpt-5-mini",
                        true,
                        900L,
                        400L,
                        true,
                        120L,
                        1,
                        80L,
                        List.of("UNDER_FILLED"),
                        4,
                        2,
                        2,
                        List.of("PARTIAL_ASSIGNMENT"),
                        300,
                        580,
                        170,
                        1200
                ));

        processFreeGameAssignmentPreviewJobService.process(job.getId());

        then(job.getStatus()).isEqualTo(AssignmentPreviewJobStatus.FAILED);
        then(job.getFailureCode()).isEqualTo(AssignmentPreviewJobFailureCode.INVALID_OUTPUT);
        then(job.getFailureMessage()).isEqualTo("AI 자동 배정 결과를 처리하지 못했어요. 다시 시도해주세요.");
        then(job.isRepairAttempted()).isTrue();
        then(job.getInitialAiElapsedMs()).isEqualTo(900L);
        then(job.getRepairAiElapsedMs()).isEqualTo(400L);
    }

    @Test
    @DisplayName("성공 로그에 attempt 및 success 이벤트를 남긴다")
    void process_whenGenerationSucceeds_logsAttemptAndSuccessEvents(CapturedOutput output) {
        AssignmentPreviewJob job = AssignmentPreviewJob.queue(UUID.randomUUID(), previewCommand(), "gpt-5-mini");
        FreeGameAssignmentPreviewGeneration generation =
                new FreeGameAssignmentPreviewGeneration(
                        previewResult(),
                        "gpt-5-mini",
                        true,
                        1000L,
                        500L,
                        true,
                        150L,
                        2,
                        90L,
                        List.of("UNDER_FILLED", "INCONSISTENT_WARNINGS"),
                        4,
                        2,
                        4,
                        List.of(),
                        300,
                        580,
                        170,
                        1200
                );

        given(manageAssignmentPreviewJobPort.findById(job.getId())).willReturn(Optional.of(job));
        given(manageAssignmentPreviewJobPort.save(any(AssignmentPreviewJob.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        given(generateFreeGameAssignmentPreviewExecutionPort.generateExecution(job.getRequestCommand()))
                .willReturn(generation);

        processFreeGameAssignmentPreviewJobService.process(job.getId());

        then(output.getOut()).contains("[ASSIGNMENT_PREVIEW_JOB] event=AI_ATTEMPT_FINISHED");
        then(output.getOut()).contains("[ASSIGNMENT_PREVIEW_JOB] event=SUCCEEDED");
        then(output.getOut()).contains("planningInputChars=300");
        then(output.getOut()).contains("promptChars=580");
        then(output.getOut()).contains("responseChars=170");
        then(output.getOut()).contains("maxCompletionTokens=1200");
        then(output.getOut()).contains("emptyResponseRetryAttempted=true");
        then(output.getOut()).contains("emptyResponseRetryElapsedMs=150");
        then(output.getOut()).contains("qualityRepairAttemptCount=2");
        then(output.getOut()).contains("qualityRepairElapsedMsTotal=90");
        then(output.getOut()).contains("qualityRepairReasons=[UNDER_FILLED, INCONSISTENT_WARNINGS]");
        then(output.getOut()).contains("theoreticalMaxFilledSlots=4");
        then(output.getOut()).contains("actualFilledSlotsAfterInitial=2");
        then(output.getOut()).contains("bestValidFilledSlots=4");
        then(output.getOut()).contains("bestValidWarningCodes=[]");
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

    private CreateFreeGameAssignmentPreviewResult previewResult() {
        return new CreateFreeGameAssignmentPreviewResult(
                List.of(
                        new CreateFreeGameAssignmentPreviewResult.Round(
                                1,
                                List.of(
                                        new CreateFreeGameAssignmentPreviewResult.Court(
                                                1,
                                                Arrays.asList(1L, 2L, null, null)
                                        )
                                )
                        )
                ),
                List.of(
                        new CreateFreeGameAssignmentPreviewResult.Warning(
                                "PARTIAL_ASSIGNMENT",
                                "일부 슬롯은 비어 있습니다."
                        )
                )
        );
    }
}
