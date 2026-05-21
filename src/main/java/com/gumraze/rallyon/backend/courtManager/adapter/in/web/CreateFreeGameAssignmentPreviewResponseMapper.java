package com.gumraze.rallyon.backend.courtManager.adapter.in.web;

import com.gumraze.rallyon.backend.courtManager.application.port.in.result.CreateFreeGameAssignmentPreviewResult;
import com.gumraze.rallyon.backend.courtManager.dto.CreateFreeGameAssignmentPreviewResponse;
import org.springframework.stereotype.Component;

@Component
public class CreateFreeGameAssignmentPreviewResponseMapper {

    public CreateFreeGameAssignmentPreviewResponse toResponse(CreateFreeGameAssignmentPreviewResult result) {
        return new CreateFreeGameAssignmentPreviewResponse(
                result.rounds().stream()
                        .map(round -> new CreateFreeGameAssignmentPreviewResponse.RoundResponse(
                                round.roundNumber(),
                                round.courts().stream()
                                        .map(court -> new CreateFreeGameAssignmentPreviewResponse.CourtResponse(
                                                court.courtNumber(),
                                                court.slots()
                                        ))
                                        .toList()
                        ))
                        .toList(),
                result.warnings().stream()
                        .map(warning -> new CreateFreeGameAssignmentPreviewResponse.WarningResponse(
                                warning.code(),
                                warning.message()
                        ))
                        .toList()
        );
    }
}
