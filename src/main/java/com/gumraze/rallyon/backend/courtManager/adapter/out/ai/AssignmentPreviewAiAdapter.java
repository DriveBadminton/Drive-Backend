package com.gumraze.rallyon.backend.courtManager.adapter.out.ai;

import com.gumraze.rallyon.backend.common.exception.ServiceUnavailableException;
import com.gumraze.rallyon.backend.courtManager.application.port.in.command.CreateFreeGameAssignmentPreviewCommand;
import com.gumraze.rallyon.backend.courtManager.application.port.in.result.CreateFreeGameAssignmentPreviewResult;
import com.gumraze.rallyon.backend.courtManager.application.port.out.GenerateFreeGameAssignmentPreviewPort;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class AssignmentPreviewAiAdapter implements GenerateFreeGameAssignmentPreviewPort {

    private final AssignmentPreviewAiClient assignmentPreviewAiClient;

    @Override
    public CreateFreeGameAssignmentPreviewResult generate(CreateFreeGameAssignmentPreviewCommand command) {
        try {

        AssignmentPreviewAiResponse response = assignmentPreviewAiClient.generate(command);
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
