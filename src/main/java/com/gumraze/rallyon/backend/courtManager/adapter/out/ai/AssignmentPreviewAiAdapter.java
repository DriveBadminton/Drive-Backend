package com.gumraze.rallyon.backend.courtManager.adapter.out.ai;

import com.gumraze.rallyon.backend.common.exception.ServiceUnavailableException;
import com.gumraze.rallyon.backend.courtManager.application.port.in.command.CreateFreeGameAssignmentPreviewCommand;
import com.gumraze.rallyon.backend.courtManager.application.port.in.result.CreateFreeGameAssignmentPreviewResult;
import com.gumraze.rallyon.backend.courtManager.application.port.out.GenerateFreeGameAssignmentPreviewExecutionPort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.GenerateFreeGameAssignmentPreviewPort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.result.FreeGameAssignmentPreviewGeneration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * AI 응답을 코트 배정 프리뷰 결과로 변환하는 outbound adapter다.
 *
 * <p>이 adapter는 AI 클라이언트 호출을 위임하고,
 * structured output을 application result로 변환한다.
 */
@Component
@RequiredArgsConstructor
public class AssignmentPreviewAiAdapter implements
        GenerateFreeGameAssignmentPreviewPort,
        GenerateFreeGameAssignmentPreviewExecutionPort {

    private final AssignmentPreviewAiGateway assignmentPreviewAiGateway;

    @Override
    public CreateFreeGameAssignmentPreviewResult generate(CreateFreeGameAssignmentPreviewCommand command) {
        try {
            return generateExecution(command).preview();
        } catch (RuntimeException ex) {
            throw new ServiceUnavailableException("AI 코트 배정 프리뷰를 현재 생성할 수 없습니다.", ex);
        }
    }

    @Override
    public FreeGameAssignmentPreviewGeneration generateExecution(
            CreateFreeGameAssignmentPreviewCommand command
    ) {
        AssignmentPreviewAiGenerationResult response = assignmentPreviewAiGateway.generateExecution(command);
        return new FreeGameAssignmentPreviewGeneration(
                toPreviewResult(response.response()),
                response.model(),
                response.repairAttempted(),
                response.initialAiElapsedMs(),
                response.repairAiElapsedMs(),
                response.emptyResponseRetryAttempted(),
                response.emptyResponseRetryElapsedMs(),
                response.qualityRepairAttemptCount(),
                response.qualityRepairElapsedMsTotal(),
                response.qualityRepairReasons(),
                response.theoreticalMaxFilledSlots(),
                response.actualFilledSlotsAfterInitial(),
                response.bestValidFilledSlots(),
                response.bestValidWarningCodes(),
                response.planningInputChars(),
                response.promptChars(),
                response.responseChars(),
                response.maxCompletionTokens()
        );
    }

    private CreateFreeGameAssignmentPreviewResult toPreviewResult(AssignmentPreviewAiResponse response) {
        return new CreateFreeGameAssignmentPreviewResult(
                response.rounds().stream()
                        .map(round -> new CreateFreeGameAssignmentPreviewResult.Round(
                                round.roundNumber(),
                                round.courts().stream()
                                        .map(court -> new CreateFreeGameAssignmentPreviewResult.Court(
                                                court.courtNumber(),
                                                court.slots()
                                        ))
                                        .toList()
                        ))
                        .toList(),
                response.warnings().stream()
                        .map(warning -> new CreateFreeGameAssignmentPreviewResult.Warning(
                                warning.code(),
                                warning.message()
                        ))
                        .toList()
        );
    }
}
