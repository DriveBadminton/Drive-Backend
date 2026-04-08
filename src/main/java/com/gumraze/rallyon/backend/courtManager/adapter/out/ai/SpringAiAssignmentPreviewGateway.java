package com.gumraze.rallyon.backend.courtManager.adapter.out.ai;

import com.gumraze.rallyon.backend.courtManager.application.port.in.command.CreateFreeGameAssignmentPreviewCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.openai.OpenAiChatModel;

@RequiredArgsConstructor
public class SpringAiAssignmentPreviewGateway implements AssignmentPreviewAiGateway {

    private final OpenAiChatModel chatModel;

    @Override
    public AssignmentPreviewAiResponse generate(CreateFreeGameAssignmentPreviewCommand command) {
        return null;
    }
}
