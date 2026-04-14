package com.gumraze.rallyon.backend.courtManager.adapter.out.persistence.repository;

import com.gumraze.rallyon.backend.courtManager.application.port.in.command.CreateFreeGameAssignmentPreviewCommand;
import com.gumraze.rallyon.backend.courtManager.application.port.in.result.CreateFreeGameAssignmentPreviewResult;
import com.gumraze.rallyon.backend.courtManager.constants.AssignmentPreviewJobStatus;
import com.gumraze.rallyon.backend.courtManager.entity.AssignmentPreviewJob;
import com.gumraze.rallyon.backend.user.constants.Gender;
import com.gumraze.rallyon.backend.user.constants.Grade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.BDDAssertions.then;

@SpringBootTest(properties = "spring.flyway.enabled=false")
@ActiveProfiles("test")
class AssignmentPreviewJobRepositoryTest {

    @Autowired
    private AssignmentPreviewJobRepository assignmentPreviewJobRepository;

    @Test
    @DisplayName("requester와 active status 기준으로 preview job 존재 여부를 조회한다")
    void existsByRequesterAccountIdAndStatusIn_returnsTrueForActiveJob() {
        UUID requesterAccountId = UUID.randomUUID();
        assignmentPreviewJobRepository.saveAndFlush(
                AssignmentPreviewJob.queue(requesterAccountId, previewCommand(), "gpt-5-mini")
        );

        boolean exists = assignmentPreviewJobRepository.existsByRequesterAccountIdAndStatusIn(
                requesterAccountId,
                List.of(AssignmentPreviewJobStatus.QUEUED, AssignmentPreviewJobStatus.RUNNING)
        );

        then(exists).isTrue();
    }

    @Test
    @DisplayName("owner 기준으로 preview job을 조회한다")
    void findByIdAndRequesterAccountId_returnsOnlyOwnedJob() {
        UUID requesterAccountId = UUID.randomUUID();
        AssignmentPreviewJob ownedJob = AssignmentPreviewJob.queue(
                requesterAccountId,
                previewCommand(),
                "gpt-5-mini"
        );
        ownedJob.markSucceeded(
                previewResult(),
                "gpt-5-mini",
                false,
                1000L,
                null,
                LocalDateTime.now(),
                1200L,
                1300L
        );
        assignmentPreviewJobRepository.saveAndFlush(ownedJob);

        then(assignmentPreviewJobRepository.findByIdAndRequesterAccountId(
                ownedJob.getId(),
                requesterAccountId
        )).isPresent();
        then(assignmentPreviewJobRepository.findByIdAndRequesterAccountId(
                ownedJob.getId(),
                UUID.randomUUID()
        )).isEmpty();
    }

    private CreateFreeGameAssignmentPreviewCommand previewCommand() {
        return new CreateFreeGameAssignmentPreviewCommand(
                List.of(
                        new CreateFreeGameAssignmentPreviewCommand.Participant(
                                "p1",
                                "서승재",
                                Gender.MALE,
                                20,
                                Grade.S,
                                1
                        ),
                        new CreateFreeGameAssignmentPreviewCommand.Participant(
                                "p2",
                                "김원호",
                                Gender.MALE,
                                20,
                                Grade.S,
                                1
                        )
                ),
                List.of(
                        new CreateFreeGameAssignmentPreviewCommand.Round(
                                1,
                                List.of(
                                        new CreateFreeGameAssignmentPreviewCommand.Court(
                                                1,
                                                Arrays.asList("p1", null, null, null)
                                        )
                                )
                        )
                ),
                List.of(
                        new CreateFreeGameAssignmentPreviewCommand.PartnerPairs("p1", "p2")
                ),
                new CreateFreeGameAssignmentPreviewCommand.Preferences(
                        CreateFreeGameAssignmentPreviewCommand.PartnerPolicy.PREFER_PARTNERS,
                        CreateFreeGameAssignmentPreviewCommand.ExistingAssignmentPolicy.FILL_EMPTY_SLOTS
                )
        );
    }

    private CreateFreeGameAssignmentPreviewResult previewResult() {
        return new CreateFreeGameAssignmentPreviewResult(
                List.of(
                        new CreateFreeGameAssignmentPreviewResult.Round(
                                1,
                                List.of(
                                        new CreateFreeGameAssignmentPreviewResult.Court(
                                                1,
                                                Arrays.asList("p1", "p2", null, null)
                                        )
                                )
                        )
                ),
                List.of()
        );
    }
}
