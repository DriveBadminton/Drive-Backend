package com.gumraze.rallyon.backend.courtManager.adapter.out.persistence.repository;

import com.gumraze.rallyon.backend.courtManager.constants.AssignmentPreviewJobStatus;
import com.gumraze.rallyon.backend.courtManager.entity.AssignmentPreviewJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssignmentPreviewJobRepository extends JpaRepository<AssignmentPreviewJob, UUID> {

    boolean existsByRequesterAccountIdAndStatusIn(
            UUID requesterAccountId,
            Collection<AssignmentPreviewJobStatus> statuses
    );

    Optional<AssignmentPreviewJob> findByIdAndRequesterAccountId(UUID id, UUID requesterAccountId);

    List<AssignmentPreviewJob> findAllByStatusIn(Collection<AssignmentPreviewJobStatus> statuses);
}
