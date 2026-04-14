package com.gumraze.rallyon.backend.courtManager.adapter.in.runtime;

import com.gumraze.rallyon.backend.courtManager.application.service.AssignmentPreviewJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AssignmentPreviewJobStartupRecovery {

    private final AssignmentPreviewJobService assignmentPreviewJobService;

    @EventListener(ApplicationReadyEvent.class)
    public void recoverInterruptedJobs() {
        assignmentPreviewJobService.recoverInterruptedJobs();
    }
}
