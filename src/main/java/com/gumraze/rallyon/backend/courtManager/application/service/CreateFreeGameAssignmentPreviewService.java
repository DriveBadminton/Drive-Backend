package com.gumraze.rallyon.backend.courtManager.application.service;

import com.gumraze.rallyon.backend.courtManager.application.port.in.CreateFreeGameAssignmentPreviewUseCase;
import com.gumraze.rallyon.backend.courtManager.application.port.in.command.CreateFreeGameAssignmentPreviewCommand;
import com.gumraze.rallyon.backend.courtManager.application.port.in.result.CreateFreeGameAssignmentPreviewResult;
import com.gumraze.rallyon.backend.courtManager.application.port.out.GenerateFreeGameAssignmentPreviewPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CreateFreeGameAssignmentPreviewService implements CreateFreeGameAssignmentPreviewUseCase {

    private final GenerateFreeGameAssignmentPreviewPort generateFreeGameAssignmentPreviewPort;

    @Override
    public CreateFreeGameAssignmentPreviewResult create(CreateFreeGameAssignmentPreviewCommand command) {
        return generateFreeGameAssignmentPreviewPort.generate(command);
    }
}
