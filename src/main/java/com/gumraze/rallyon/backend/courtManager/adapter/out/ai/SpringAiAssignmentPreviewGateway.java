package com.gumraze.rallyon.backend.courtManager.adapter.out.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.gumraze.rallyon.backend.courtManager.application.port.in.command.CreateFreeGameAssignmentPreviewCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.ResponseFormat;
import java.util.Map;

@RequiredArgsConstructor
public class SpringAiAssignmentPreviewGateway implements AssignmentPreviewAiGateway {

    private static final String ASSIGNMENT_PREVIEW_PROMPT = """
            다음 자유게임 상태를 기준으로 코트 배정 프리뷰를 생성하세요.
            결과는 rounds와 warnings를 포함한 JSON만 반환하세요.
            입력 데이터:
            """;


    private final OpenAiChatModel chatModel;
    private final ObjectMapper objectMapper;

    @Override
    public AssignmentPreviewAiResponse generate(
            CreateFreeGameAssignmentPreviewCommand command
    ) {
        Map<String, Object> schema;
        String promptText;
        try {
            schema = objectMapper.readValue(
                    AssignmentPreviewAiSchema.ASSIGNMENT_PREVIEW_JSON_SCHEMA,
                    new TypeReference<Map<String, Object>>() {
                    }
            );
            promptText = ASSIGNMENT_PREVIEW_PROMPT + objectMapper.writeValueAsString(command);

        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("OpenAI로부터 응답을 읽을 수 없습니다.", ex);
        }


        // OpenAi 호출 시 사용할 옵션 객체
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                // 응답 형식 지정
                .responseFormat(ResponseFormat.builder()
                        // 모델 응답 형식을 JSON_SCHEMA로 강제함
                        .type(ResponseFormat.Type.JSON_SCHEMA)
                        // JSON_SCHEMA 관련 세부 설정
                        .jsonSchema(ResponseFormat.JsonSchema.builder()
                                .name("assignment_preview")     // 스키마 식별용 이름 설정
                                .schema(schema)                 // 스키마 설정
                                .strict(true)                   // 스키마 설정 엄격
                                .build())
                        .build())
                .build();

        // OpenAI 호출 수행, 결과는 ChatResponse로 받음
        ChatResponse response = chatModel.call(new Prompt(promptText, options));

        if (response.getResult() == null || response.getResult().getOutput() == null) {
            throw new IllegalStateException("OpenAI 응답이 비어 있습니다.");
        }

        String responseText = response.getResult().getOutput().getText();
        if (responseText == null || responseText.isBlank()) {
            throw new IllegalStateException("OpenAI 응답이 비어 있습니다.");
        }

        try {
            return objectMapper.readValue(
                    responseText,
                    AssignmentPreviewAiResponse.class
            );
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("OpenAI로부터 응답을 읽을 수 없습니다.", ex);
        }
    }
}
