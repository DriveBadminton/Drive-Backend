package com.gumraze.rallyon.backend.courtManager.application.port.out;

import com.gumraze.rallyon.backend.courtManager.application.port.in.command.CreateFreeGameAssignmentPreviewCommand;
import com.gumraze.rallyon.backend.courtManager.application.port.in.result.CreateFreeGameAssignmentPreviewResult;

public interface GenerateFreeGameAssignmentPreviewPort {

    CreateFreeGameAssignmentPreviewResult generate(CreateFreeGameAssignmentPreviewCommand command);

}
