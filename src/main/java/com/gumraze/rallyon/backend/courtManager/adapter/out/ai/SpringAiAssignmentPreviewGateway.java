package com.gumraze.rallyon.backend.courtManager.adapter.out.ai;

import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.gumraze.rallyon.backend.courtManager.application.port.in.command.CreateFreeGameAssignmentPreviewCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.ResponseFormat;
import org.springframework.stereotype.Component;
import java.util.HashSet;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SpringAiAssignmentPreviewGateway implements AssignmentPreviewAiGateway {

    private final AssignmentPreviewPlanningInputMapper planningInputMapper;

    private static final String INVALID_OUTPUT_MESSAGE = "OpenAI 응답 구조가 요청과 일치하지 않습니다.";
    private static final String ASSIGNMENT_PREVIEW_PROMPT = """
            다음 자유게임 상태를 기준으로 코트 배정 프리뷰를 생성하세요.
            아래 우선순위를 순서대로 지키세요.
            1. 입력과 동일한 rounds / courts 구조를 유지하세요.
            2. 같은 라운드 안에 동일 참가자를 두 번 배정하지 마세요.
            3. constraintGuidance.preserveFixedSlots=true 이면 fixed=true 슬롯의 participantId를 절대 변경하지 마세요.
            4. policyGuidance.fillEmptySlotsOnly=true 이면 기존 non-null 슬롯을 유지한 채 null 슬롯을 최대한 채우세요.
            5. partnerGuidance.preferProvidedPartnerPairs=true 이면 가능한 범위에서 partnerPairs를 우선 반영하세요.
            6. 추가로 배정할 수 없는 슬롯이 남으면 warnings에 PARTIAL_ASSIGNMENT를 포함하세요.
            7. 파트너 선호를 완전히 만족하지 못하면 warnings에 PARTNER_CONSTRAINT_PARTIAL를 포함하세요.
            8. 배정을 개선할 수 없어 기존 상태를 유지한 경우에도 warnings를 비워두지 마세요.
            결과는 rounds와 warnings를 포함한 JSON만 반환하세요.
            입력 데이터:
            """;

    private static final String ASSIGNMENT_PREVIEW_REPAIR_PROMPT = """
            이전 응답은 요청 구조와 일치하지 않았습니다.
            이번에는 아래 규칙을 모두 만족하도록 다시 생성하세요.
            1. 입력과 동일한 rounds / courts 구조를 반드시 유지하세요.
            2. 같은 라운드 안에 동일 참가자를 두 번 배정하지 마세요.
            3. constraintGuidance.preserveFixedSlots=true 이면 fixed=true 슬롯의 participantId를 절대 변경하지 마세요.
            4. policyGuidance.fillEmptySlotsOnly=true 이면 기존 non-null 슬롯을 유지한 채 null 슬롯을 최대한 채우세요.
            5. partnerGuidance.preferProvidedPartnerPairs=true 이면 가능한 범위에서 partnerPairs를 우선 반영하세요.
            6. 추가로 배정할 수 없는 슬롯이 남으면 warnings에 PARTIAL_ASSIGNMENT를 포함하세요.
            7. 파트너 선호를 완전히 만족하지 못하면 warnings에 PARTNER_CONSTRAINT_PARTIAL를 포함하세요.
            8. 배정을 개선할 수 없어 기존 상태를 유지한 경우에도 warnings를 비워두지 마세요.
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
            promptText = ASSIGNMENT_PREVIEW_PROMPT + serializePlanningInput(command);

        } catch (JacksonException ex) {
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

        AssignmentPreviewAiResponse aiResponse = requestAssignmentPreview(promptText, options);

        try {
            validateRoundStructure(command, aiResponse);
            return aiResponse;
        } catch (IllegalStateException ex) {
            if (!INVALID_OUTPUT_MESSAGE.equals(ex.getMessage())) {
                throw ex;
            }
            AssignmentPreviewAiResponse repairedResponse =
                    requestAssignmentPreview(buildRepairPrompt(command), options);
            validateRoundStructure(command, repairedResponse);
            return repairedResponse;
        }
    }

    private void validateRoundStructure(
            CreateFreeGameAssignmentPreviewCommand command,
            AssignmentPreviewAiResponse response
    ) {
        if (command == null) {
            return;
        }

        validateResponseShapeNotNull(response);
        validateWarningsShape(response);
        validateParticipantIds(command, response);

        if (command.rounds().size() != response.rounds().size()) {
            throw invalidOutput();
        }

        for (int i = 0; i < command.rounds().size(); i++) {
            CreateFreeGameAssignmentPreviewCommand.Round requestedRound =
                    command.rounds().get(i);
            AssignmentPreviewAiResponse.Round responseRound =
                    response.rounds().get(i);

            validateRound(requestedRound, responseRound);

            if (command.preferences().existingAssignmentPolicy()
                    == CreateFreeGameAssignmentPreviewCommand.ExistingAssignmentPolicy.FILL_EMPTY_SLOTS) {
                validateFixedSlots(requestedRound, responseRound);
            }
        }
    }

    private void validateRound(
            CreateFreeGameAssignmentPreviewCommand.Round requestedRound,
            AssignmentPreviewAiResponse.Round responseRound
    ) {
        if (!requestedRound.roundNumber().equals(responseRound.roundNumber())) {
            throw invalidOutput();
        }

        if (requestedRound.courts().size() != responseRound.courts().size()) {
            throw invalidOutput();
        }

        validateDuplicateParticipantsInRound(responseRound);
        validateCourtStructure(requestedRound, responseRound);
    }

    private void validateCourtStructure(
            CreateFreeGameAssignmentPreviewCommand.Round requestedRound,
            AssignmentPreviewAiResponse.Round responseRound
    ) {
        for (int j = 0; j < requestedRound.courts().size(); j++) {
            CreateFreeGameAssignmentPreviewCommand.Court requestedCourt =
                    requestedRound.courts().get(j);
            AssignmentPreviewAiResponse.Court responseCourt =
                    responseRound.courts().get(j);

            if (!requestedCourt.courtNumber().equals(responseCourt.courtNumber())) {
                throw invalidOutput();
            }

            if (responseCourt.slots().size() != 4) {
                throw invalidOutput();
            }
        }
    }

    private void validateFixedSlots(
            CreateFreeGameAssignmentPreviewCommand.Round requestedRound,
            AssignmentPreviewAiResponse.Round responseRound
    ) {
        for (int i = 0; i < requestedRound.courts().size(); i++) {
            CreateFreeGameAssignmentPreviewCommand.Court requestedCourt =
                    requestedRound.courts().get(i);
            AssignmentPreviewAiResponse.Court responseCourt =
                    responseRound.courts().get(i);

            List<String> requestedSlots = requestedCourt.slots();
            List<String> responseSlots = responseCourt.slots();

            for (int j = 0; j < requestedSlots.size(); j++) {
                String requestedSlot = requestedSlots.get(j);
                String responseSlot = responseSlots.get(j);

                if (requestedSlot != null && !requestedSlot.equals(responseSlot)) {
                    throw invalidOutput();
                }
            }
        }
    }

    private void validateParticipantIds(
            CreateFreeGameAssignmentPreviewCommand command,
            AssignmentPreviewAiResponse response
    ) {
        Set<String> participantIds = command.participants().stream()
                .map(CreateFreeGameAssignmentPreviewCommand.Participant::clientId)
                .collect(Collectors.toSet());

        for (AssignmentPreviewAiResponse.Round round : response.rounds()) {
            for (AssignmentPreviewAiResponse.Court court : round.courts()) {
                for (String slot : court.slots()) {
                    if (slot != null && !participantIds.contains(slot)) {
                        throw invalidOutput();
                    }
                }
            }
        }
    }

    private void validateDuplicateParticipantsInRound(AssignmentPreviewAiResponse.Round responseRound) {
        Set<String> assignedParticipants = new HashSet<>();

        for (AssignmentPreviewAiResponse.Court court : responseRound.courts()) {
            for (String slot : court.slots()) {
                if (slot != null && !assignedParticipants.add(slot)) {
                    throw invalidOutput();
                }
            }
        }
    }

    private AssignmentPreviewAiResponse requestAssignmentPreview(
            String promptText,
            OpenAiChatOptions options
    ) {
        ChatResponse response = chatModel.call(new Prompt(promptText, options));

        if (response.getResult() == null || response.getResult().getOutput() == null) {
            throw new IllegalStateException("OpenAI 응답이 비어 있습니다.");
        }

        String responseText = response.getResult().getOutput().getText();
        if (responseText == null || responseText.isBlank()) {
            throw new IllegalStateException("OpenAI 응답이 비어 있습니다.");
        }

        try {
            return objectMapper.readValue(responseText, AssignmentPreviewAiResponse.class);
        } catch (JacksonException ex) {
            throw new IllegalStateException("OpenAI로부터 응답을 읽을 수 없습니다.", ex);
        }
    }

    private String buildRepairPrompt(CreateFreeGameAssignmentPreviewCommand command) {
        try {
            return ASSIGNMENT_PREVIEW_REPAIR_PROMPT + serializePlanningInput(command);
        } catch (JacksonException ex) {
            throw new IllegalStateException("OpenAI로부터 응답을 읽을 수 없습니다.", ex);
        }
    }

    private String serializePlanningInput(CreateFreeGameAssignmentPreviewCommand command)
            throws JacksonException {
        if (command == null) {
            return objectMapper.writeValueAsString(null);
        }

        AssignmentPreviewPlanningInput planningInput = planningInputMapper.from(command);
        return objectMapper.writeValueAsString(planningInput);
    }

    private void validateResponseShapeNotNull(AssignmentPreviewAiResponse response) {
        if (response.rounds() == null) {
            throw invalidOutput();
        }

        for (AssignmentPreviewAiResponse.Round round : response.rounds()) {
            if (round == null || round.roundNumber() == null || round.courts() == null) {
                throw invalidOutput();
            }

            for (AssignmentPreviewAiResponse.Court court : round.courts()) {
                if (court == null || court.courtNumber() == null || court.slots() == null) {
                    throw invalidOutput();
                }
            }
        }
    }

    private void validateWarningsShape(AssignmentPreviewAiResponse response) {
        if (response.warnings() == null) {
            throw invalidOutput();
        }

        for (AssignmentPreviewAiResponse.Warning warning : response.warnings()) {
            if (warning == null || warning.code() == null || warning.message() == null) {
                throw invalidOutput();
            }
        }
    }

    private IllegalStateException invalidOutput() {
        return new IllegalStateException(INVALID_OUTPUT_MESSAGE);
    }
    
}
