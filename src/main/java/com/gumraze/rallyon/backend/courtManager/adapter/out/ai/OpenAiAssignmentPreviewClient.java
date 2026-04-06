package com.gumraze.rallyon.backend.courtManager.adapter.out.ai;

import com.gumraze.rallyon.backend.courtManager.application.port.in.command.CreateFreeGameAssignmentPreviewCommand;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OpenAiAssignmentPreviewClient implements AssignmentPreviewAiClient {

    private final OpenAiAssignmentPreviewGateway gateway;

    @Override
    public AssignmentPreviewAiResponse generate(CreateFreeGameAssignmentPreviewCommand command) {
        return gateway.generate(command);
    }
}
