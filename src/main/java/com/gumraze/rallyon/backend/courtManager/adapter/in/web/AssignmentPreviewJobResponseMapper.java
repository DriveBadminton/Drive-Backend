package com.gumraze.rallyon.backend.courtManager.adapter.in.web;

import com.gumraze.rallyon.backend.courtManager.application.port.in.result.GetFreeGameAssignmentPreviewJobStatusResult;
import com.gumraze.rallyon.backend.courtManager.application.port.in.result.SubmitFreeGameAssignmentPreviewJobResult;
import com.gumraze.rallyon.backend.courtManager.dto.CreateFreeGameAssignmentPreviewJobResponse;
import com.gumraze.rallyon.backend.courtManager.dto.GetFreeGameAssignmentPreviewJobResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AssignmentPreviewJobResponseMapper {

    private final CreateFreeGameAssignmentPreviewResponseMapper previewResponseMapper;

    public CreateFreeGameAssignmentPreviewJobResponse toSubmitResponse(
            SubmitFreeGameAssignmentPreviewJobResult result
    ) {
        return new CreateFreeGameAssignmentPreviewJobResponse(
                result.jobId(),
                result.status().name(),
                result.pollAfterMs()
        );
    }

    public GetFreeGameAssignmentPreviewJobResponse toStatusResponse(
            GetFreeGameAssignmentPreviewJobStatusResult result
    ) {
        return new GetFreeGameAssignmentPreviewJobResponse(
                result.jobId(),
                result.status().name(),
                result.preview() == null ? null : previewResponseMapper.toResponse(result.preview()),
                result.failure() == null ? null : new GetFreeGameAssignmentPreviewJobResponse.FailureResponse(
                        result.failure().code(),
                        result.failure().message()
                ),
                result.submittedAt(),
                result.startedAt(),
                result.completedAt()
        );
    }
}
