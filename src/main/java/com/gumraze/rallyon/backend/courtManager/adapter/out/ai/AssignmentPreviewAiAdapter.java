package com.gumraze.rallyon.backend.courtManager.adapter.out.ai;

import com.gumraze.rallyon.backend.common.exception.ServiceUnavailableException;
import com.gumraze.rallyon.backend.courtManager.application.port.in.command.CreateFreeGameAssignmentPreviewCommand;
import com.gumraze.rallyon.backend.courtManager.application.port.in.result.CreateFreeGameAssignmentPreviewResult;
import com.gumraze.rallyon.backend.courtManager.application.port.out.GenerateFreeGameAssignmentPreviewPort;
import lombok.RequiredArgsConstructor;

/**
 * AI 응답을 코트 배정 프리뷰 결과로 변환하는 outbound adapter다.
 *
 * <p>이 adapter는 AI 클라이언트 호출을 위임하고,
 * structured output을 application result로 변환한다.
 */
@RequiredArgsConstructor
public class AssignmentPreviewAiAdapter implements GenerateFreeGameAssignmentPreviewPort {

    private final AssignmentPreviewAiGateway assignmentPreviewAiGateway;

    @Override
    public CreateFreeGameAssignmentPreviewResult generate(CreateFreeGameAssignmentPreviewCommand command) {
        try {

            AssignmentPreviewAiResponse response = assignmentPreviewAiGateway.generate(command);
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
        } catch (RuntimeException ex) {
            throw new ServiceUnavailableException("AI 코트 배정 프리뷰를 현재 생성할 수 없습니다.", ex);
        }
    }
}
