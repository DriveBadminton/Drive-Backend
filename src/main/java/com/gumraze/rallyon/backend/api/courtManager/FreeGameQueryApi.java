package com.gumraze.rallyon.backend.api.courtManager;

import com.gumraze.rallyon.backend.api.common.ApiBearerAuth;
import com.gumraze.rallyon.backend.courtManager.dto.FreeGameDetailResponse;
import com.gumraze.rallyon.backend.courtManager.dto.FreeGameRoundMatchResponse;
import com.gumraze.rallyon.backend.courtManager.dto.GetFreeGameAssignmentPreviewJobResponse;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

@ApiBearerAuth
public interface FreeGameQueryApi {

    ResponseEntity<FreeGameDetailResponse> getFreeGameDetail(
            UUID accountId,
            UUID gameId
    );

    ResponseEntity<FreeGameRoundMatchResponse> getFreeGameRoundMatchResponse(
            UUID accountId,
            UUID gameId
    );

    ResponseEntity<GetFreeGameAssignmentPreviewJobResponse> getFreeGameAssignmentPreviewStatus(
            UUID accountId,
            UUID jobId
    );
}
