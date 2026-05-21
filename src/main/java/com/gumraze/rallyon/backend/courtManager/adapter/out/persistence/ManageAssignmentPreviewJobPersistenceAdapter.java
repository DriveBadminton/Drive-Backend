package com.gumraze.rallyon.backend.courtManager.adapter.out.persistence;

import com.gumraze.rallyon.backend.courtManager.adapter.out.persistence.repository.AssignmentPreviewJobRepository;
import com.gumraze.rallyon.backend.courtManager.application.port.out.ManageAssignmentPreviewJobPort;
import com.gumraze.rallyon.backend.courtManager.constants.AssignmentPreviewJobStatus;
import com.gumraze.rallyon.backend.courtManager.entity.AssignmentPreviewJob;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ManageAssignmentPreviewJobPersistenceAdapter implements ManageAssignmentPreviewJobPort {

    private final AssignmentPreviewJobRepository assignmentPreviewJobRepository;

    @Override
    public boolean existsByRequesterAccountIdAndStatusIn(
            UUID requesterAccountId,
            Collection<AssignmentPreviewJobStatus> statuses
    ) {
        return assignmentPreviewJobRepository.existsByRequesterAccountIdAndStatusIn(
                requesterAccountId,
                statuses
        );
    }

    @Override
    public AssignmentPreviewJob save(AssignmentPreviewJob job) {
        return assignmentPreviewJobRepository.saveAndFlush(job);
    }

    @Override
    public Optional<AssignmentPreviewJob> findById(UUID jobId) {
        return assignmentPreviewJobRepository.findById(jobId);
    }

    @Override
    public Optional<AssignmentPreviewJob> findByIdAndRequesterAccountId(UUID jobId, UUID requesterAccountId) {
        return assignmentPreviewJobRepository.findByIdAndRequesterAccountId(jobId, requesterAccountId);
    }

    @Override
    public List<AssignmentPreviewJob> findAllByStatusIn(Collection<AssignmentPreviewJobStatus> statuses) {
        return assignmentPreviewJobRepository.findAllByStatusIn(statuses);
    }
}
