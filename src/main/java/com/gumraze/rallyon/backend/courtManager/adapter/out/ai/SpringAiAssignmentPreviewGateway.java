package com.gumraze.rallyon.backend.courtManager.adapter.out.ai;

import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.gumraze.rallyon.backend.common.exception.ServiceUnavailableException;
import com.gumraze.rallyon.backend.courtManager.application.port.in.command.CreateFreeGameAssignmentPreviewCommand;
import com.gumraze.rallyon.backend.courtManager.constants.AssignmentPreviewJobFailureCode;
import org.apache.hc.client5.http.ConnectTimeoutException;
import lombok.Getter;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.ResponseFormat;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.net.http.HttpTimeoutException;
import java.util.HashSet;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Component
public class SpringAiAssignmentPreviewGateway implements AssignmentPreviewAiGateway {

    private static final String INVALID_OUTPUT_MESSAGE = "OpenAI 응답 구조가 요청과 일치하지 않습니다.";
    private static final String REPEATED_ROUND_LAYOUT_MESSAGE =
            "OpenAI 응답이 동일한 round layout을 여러 라운드에 반복했습니다.";
    private static final String EMPTY_RESPONSE_MESSAGE = "OpenAI 응답이 비어 있습니다.";
    private static final String OPENAI_TIMEOUT_MESSAGE =
            "AI 자동 배정 응답 시간이 초과되었습니다. 잠시 후 다시 시도해주세요.";
    private static final Map<String, String> WARNING_MESSAGE_BY_CODE = Map.of(
            "PARTIAL_ASSIGNMENT", "일부 슬롯은 비어 있습니다.",
            "PARTNER_CONSTRAINT_PARTIAL", "일부 파트너 조합을 완전히 반영하지 못했습니다.",
            "NO_ACTION_NEEDED", "추가로 조정할 배정이 없습니다."
    );
    private static final Double PREVIEW_TEMPERATURE = 0.0d;
    private static final String ASSIGNMENT_PREVIEW_PROMPT = """
            Generate a JSON preview for free-game court assignments.
            Rules:
            - Preserve the exact round and court structure from the input.
            - Each court must return exactly 4 slots.
            - Always return warnings as an array. Use [] when there are no warnings.
            - Participant ids are numeric aliases. Return them exactly as provided.
            - Use each participant id at most once per round.
            - Primary objective: maximize filled slots.
            - If a null slot can be filled without breaking constraints, fill it. Do not stop early.
            - Do not copy an entire previous round layout.
            - Try to vary court-level 4-player layouts across rounds.
            - If guidance.preserveFixedSlots is true, keep fixed slots unchanged.
            - If guidance.fillEmptySlotsOnly is true, keep existing non-null slots unchanged and fill only null slots.
            - If guidance.preferProvidedPartnerPairs is true, try to place each partner pair on the same court.
            - If fixed slots or fill-empty-only rules make a partner pair impossible, keep the existing assignments and add PARTNER_CONSTRAINT_PARTIAL.
            - Example: if two preferred partners are fixed on different courts, keep them fixed and add PARTNER_CONSTRAINT_PARTIAL.
            - If empty slots remain, include PARTIAL_ASSIGNMENT.
            - If some preferred partner pairs cannot be satisfied, include PARTNER_CONSTRAINT_PARTIAL.
            - Use NO_FURTHER_IMPROVEMENT only when no additional null slot can be filled without breaking constraints.
            - If no empty slots remain, do not include PARTIAL_ASSIGNMENT or NO_FURTHER_IMPROVEMENT.
            - Return JSON only with rounds and warnings.
            - Keep warning messages short and in Korean.
            Input:
            """;
    private static final String EMPTY_RESPONSE_RETRY_PROMPT_SUFFIX = """

            The previous response was empty. Return a non-empty JSON object that matches the schema exactly.
            """;
    private static final String ASSIGNMENT_PREVIEW_QUALITY_REPAIR_PROMPT = """
            The previous output was structurally valid but still needs improvement.
            %s
            Issues to fix:
            %s
            Rules:
            - Preserve the exact round and court structure from the input.
            - Each court must return exactly 4 slots.
            - Preserve all fixed slots and all existing non-null assignments from the input.
            - Primary objective: maximize filled slots.
            - If a null slot can be filled without breaking constraints, fill it. Do not stop early.
            - For FILL_EMPTY_SLOTS, preserving existing non-null slots does not mean leaving null slots empty.
            - You may rearrange participants assigned only to previously null slots if it increases total filled slots.
            - Always return warnings as an array. Use [] when there are no warnings.
            - If empty slots remain, include PARTIAL_ASSIGNMENT or NO_FURTHER_IMPROVEMENT.
            - If no empty slots remain, do not include PARTIAL_ASSIGNMENT or NO_FURTHER_IMPROVEMENT.
            - Keep warnings consistent with the final fill coverage.
            - Participant ids are numeric aliases. Return them exactly as provided.
            - Return JSON only with rounds and warnings.
            Planning input:
            %s

            Previous output:
            %s
            """;

    private static final String ASSIGNMENT_PREVIEW_REPAIR_PROMPT = """
            The previous response did not satisfy the required structure or constraints.
            Failure reason: %s
            Generate the preview again.
            Rules:
            - Preserve the exact round and court structure from the input.
            - Each court must return exactly 4 slots.
            - Always return warnings as an array. Use [] when there are no warnings.
            - Participant ids are numeric aliases. Return them exactly as provided.
            - Use each participant id at most once per round.
            - Primary objective: maximize filled slots.
            - If a null slot can be filled without breaking constraints, fill it. Do not stop early.
            - Do not copy an entire previous round layout.
            - Try to vary court-level 4-player layouts across rounds.
            - If guidance.preserveFixedSlots is true, keep fixed slots unchanged.
            - If guidance.fillEmptySlotsOnly is true, keep existing non-null slots unchanged and fill only null slots.
            - If guidance.preferProvidedPartnerPairs is true, try to place each partner pair on the same court.
            - If fixed slots or fill-empty-only rules make a partner pair impossible, keep the existing assignments and add PARTNER_CONSTRAINT_PARTIAL.
            - Example: if two preferred partners are fixed on different courts, keep them fixed and add PARTNER_CONSTRAINT_PARTIAL.
            - If empty slots remain, include PARTIAL_ASSIGNMENT.
            - If some preferred partner pairs cannot be satisfied, include PARTNER_CONSTRAINT_PARTIAL.
            - Use NO_FURTHER_IMPROVEMENT only when no additional null slot can be filled without breaking constraints.
            - If no empty slots remain, do not include PARTIAL_ASSIGNMENT or NO_FURTHER_IMPROVEMENT.
            - Return JSON only with rounds and warnings.
            - Keep warning messages short and in Korean.
            Input:
            """;

    private final AssignmentPreviewPlanningInputMapper planningInputMapper;
    private final OpenAiChatModel chatModel;
    private final ObjectMapper objectMapper;
    private final AssignmentPreviewAiProperties assignmentPreviewAiProperties;

    @Autowired
    public SpringAiAssignmentPreviewGateway(
            AssignmentPreviewPlanningInputMapper planningInputMapper,
            @Qualifier("assignmentPreviewChatModel") OpenAiChatModel chatModel,
            ObjectMapper objectMapper,
            AssignmentPreviewAiProperties assignmentPreviewAiProperties
    ) {
        this.planningInputMapper = planningInputMapper;
        this.chatModel = chatModel;
        this.objectMapper = objectMapper;
        this.assignmentPreviewAiProperties = assignmentPreviewAiProperties;
    }

    SpringAiAssignmentPreviewGateway(
            AssignmentPreviewPlanningInputMapper planningInputMapper,
            OpenAiChatModel chatModel,
            ObjectMapper objectMapper
    ) {
        this(
                planningInputMapper,
                chatModel,
                objectMapper,
                AssignmentPreviewAiProperties.defaults()
        );
    }

    @Override
    public AssignmentPreviewAiGenerationResult generateExecution(
            CreateFreeGameAssignmentPreviewCommand command
    ) {
        AssignmentPreviewPromptPayload promptPayload = planningInputMapper.from(command);
        Map<String, Object> schema;
        String planningInputJson;
        String promptText;
        String model = assignmentPreviewAiProperties.getModel();
        Integer maxCompletionTokens = assignmentPreviewAiProperties.getMaxCompletionTokens();
        Integer theoreticalMaxFilledSlots = theoreticalMaxFilledSlots(command);
        int planningInputChars;
        int promptChars;
        try {
            schema = objectMapper.readValue(
                    AssignmentPreviewAiSchema.ASSIGNMENT_PREVIEW_JSON_SCHEMA,
                    new TypeReference<Map<String, Object>>() {
                    }
            );
            planningInputJson = serializePlanningInput(promptPayload);
            planningInputChars = planningInputJson.length();
            promptText = ASSIGNMENT_PREVIEW_PROMPT + planningInputJson;
            promptChars = promptText.length();
        } catch (JacksonException ex) {
            throw invalidResponse(
                    "OpenAI로부터 응답을 읽을 수 없습니다.",
                    ex,
                    model,
                    false,
                    null,
                    null,
                    false,
                    null,
                    0,
                    null,
                    List.of(),
                    theoreticalMaxFilledSlots,
                    null,
                    null,
                    List.of(),
                    null,
                    null,
                    null,
                    maxCompletionTokens
            );
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
        options.setMaxCompletionTokens(maxCompletionTokens);
        applySamplingOptions(options, model);

        Long initialAiElapsedMs = null;
        Long repairAiElapsedMs = null;
        boolean emptyResponseRetryAttempted = false;
        Long emptyResponseRetryElapsedMs = null;
        Integer actualFilledSlotsAfterInitial = null;
        Integer bestValidFilledSlots = null;
        List<String> bestValidWarningCodes = List.of();
        int qualityRepairAttemptCount = 0;
        long qualityRepairElapsedMsTotal = 0L;
        List<String> qualityRepairReasons = new ArrayList<>();
        int effectivePromptChars = promptChars;
        boolean repairAttempted = false;

        RequestedPreview aiResponse;
        long initialStartNanos = System.nanoTime();
        try {
            aiResponse = requestAssignmentPreview(promptText, options);
            initialAiElapsedMs = elapsedMillis(initialStartNanos);
        } catch (EmptyResponseException ex) {
            initialAiElapsedMs = elapsedMillis(initialStartNanos);
            emptyResponseRetryAttempted = true;
            String emptyResponseRetryPrompt = buildEmptyResponseRetryPrompt(promptText);
            effectivePromptChars = emptyResponseRetryPrompt.length();
            long emptyResponseRetryStartNanos = System.nanoTime();
            try {
                aiResponse = requestAssignmentPreview(emptyResponseRetryPrompt, options);
            } catch (RuntimeException retryEx) {
                emptyResponseRetryElapsedMs = elapsedMillis(emptyResponseRetryStartNanos);
                throw wrapFailure(
                        retryEx,
                        model,
                        false,
                        initialAiElapsedMs,
                        null,
                        emptyResponseRetryAttempted,
                        emptyResponseRetryElapsedMs,
                        0,
                        null,
                        List.of(),
                        theoreticalMaxFilledSlots,
                        null,
                        null,
                        List.of(),
                        planningInputChars,
                        effectivePromptChars,
                        null,
                        maxCompletionTokens
                );
            }
            emptyResponseRetryElapsedMs = elapsedMillis(emptyResponseRetryStartNanos);
        } catch (RuntimeException ex) {
            initialAiElapsedMs = elapsedMillis(initialStartNanos);
            throw wrapFailure(
                    ex,
                    model,
                    false,
                    initialAiElapsedMs,
                    null,
                    emptyResponseRetryAttempted,
                    emptyResponseRetryElapsedMs,
                    0,
                    null,
                    List.of(),
                    theoreticalMaxFilledSlots,
                    null,
                    null,
                    List.of(),
                    planningInputChars,
                    effectivePromptChars,
                    null,
                    maxCompletionTokens
            );
        }

        RequestedPreview firstValidPreview = aiResponse;
        int bestValidPromptChars = effectivePromptChars;
        try {
            validateRoundStructure(command, promptPayload, aiResponse.response());
        } catch (IllegalStateException ex) {
            if (!isRepairableInvalidOutput(ex)) {
                throw wrapFailure(
                        ex,
                        model,
                        false,
                        initialAiElapsedMs,
                        null,
                        emptyResponseRetryAttempted,
                        emptyResponseRetryElapsedMs,
                        0,
                        0L,
                        List.of(),
                        theoreticalMaxFilledSlots,
                        null,
                        null,
                        List.of(),
                        planningInputChars,
                        effectivePromptChars,
                        aiResponse.responseChars(),
                        maxCompletionTokens
                );
            }

            repairAttempted = true;
            String repairPrompt = buildRepairPrompt(planningInputJson, ex.getMessage());
            int repairPromptChars = repairPrompt.length();
            RequestedPreview repairedResponse;
            long repairStartNanos = System.nanoTime();
            try {
                repairedResponse = requestAssignmentPreview(repairPrompt, options);
            } catch (RuntimeException repairEx) {
                repairAiElapsedMs = elapsedMillis(repairStartNanos);
                throw wrapFailure(
                        repairEx,
                        model,
                        true,
                        initialAiElapsedMs,
                        repairAiElapsedMs,
                        emptyResponseRetryAttempted,
                        emptyResponseRetryElapsedMs,
                        0,
                        0L,
                        List.of(),
                        theoreticalMaxFilledSlots,
                        null,
                        null,
                        List.of(),
                        planningInputChars,
                        repairPromptChars,
                        null,
                        maxCompletionTokens
                );
            }
            repairAiElapsedMs = elapsedMillis(repairStartNanos);
            try {
                validateRoundStructure(command, promptPayload, repairedResponse.response());
            } catch (IllegalStateException repairValidationEx) {
                throw wrapFailure(
                        repairValidationEx,
                        model,
                        true,
                        initialAiElapsedMs,
                        repairAiElapsedMs,
                        emptyResponseRetryAttempted,
                        emptyResponseRetryElapsedMs,
                        0,
                        0L,
                        List.of(),
                        theoreticalMaxFilledSlots,
                        null,
                        null,
                        List.of(),
                        planningInputChars,
                        repairPromptChars,
                        repairedResponse.responseChars(),
                        maxCompletionTokens
                );
            }
            firstValidPreview = repairedResponse;
            bestValidPromptChars = repairPromptChars;
        }

        QualityEvaluation bestQuality = evaluateQuality(command, firstValidPreview.response(), theoreticalMaxFilledSlots);
        actualFilledSlotsAfterInitial = bestQuality.filledSlots();
        bestValidFilledSlots = bestQuality.filledSlots();
        bestValidWarningCodes = bestQuality.normalizedWarningCodes();
        RequestedPreview bestValidPreview = firstValidPreview;

        while (qualityRepairAttemptCount < 2 && bestQuality.hasDeficiencies()) {
            mergeQualityRepairReasons(qualityRepairReasons, bestQuality.deficiencyNames());
            int nextAttempt = qualityRepairAttemptCount + 1;
            String qualityRepairPrompt = buildQualityRepairPrompt(
                    planningInputJson,
                    bestValidPreview.response(),
                    bestQuality,
                    nextAttempt
            );
            int qualityRepairPromptChars = qualityRepairPrompt.length();
            long qualityRepairStartNanos = System.nanoTime();
            try {
                RequestedPreview repairedQualityPreview = requestAssignmentPreview(qualityRepairPrompt, options);
                long elapsedMs = elapsedMillis(qualityRepairStartNanos);
                qualityRepairElapsedMsTotal += elapsedMs;
                qualityRepairAttemptCount = nextAttempt;

                validateRoundStructure(command, promptPayload, repairedQualityPreview.response());
                QualityEvaluation repairedQuality = evaluateQuality(
                        command,
                        repairedQualityPreview.response(),
                        theoreticalMaxFilledSlots
                );

                if (isBetterCandidate(bestQuality, repairedQuality)) {
                    bestValidPreview = repairedQualityPreview;
                    bestQuality = repairedQuality;
                    bestValidFilledSlots = repairedQuality.filledSlots();
                    bestValidWarningCodes = repairedQuality.normalizedWarningCodes();
                    bestValidPromptChars = qualityRepairPromptChars;
                }
            } catch (RuntimeException ignored) {
                qualityRepairElapsedMsTotal += elapsedMillis(qualityRepairStartNanos);
                qualityRepairAttemptCount = nextAttempt;
            }
        }

        AssignmentPreviewAiResponse mappedResponse = remapResponse(bestValidPreview.response(), promptPayload);
        return new AssignmentPreviewAiGenerationResult(
                mappedResponse,
                model,
                repairAttempted,
                initialAiElapsedMs,
                repairAiElapsedMs,
                emptyResponseRetryAttempted,
                emptyResponseRetryElapsedMs,
                qualityRepairAttemptCount,
                qualityRepairAttemptCount == 0 ? null : qualityRepairElapsedMsTotal,
                List.copyOf(qualityRepairReasons),
                theoreticalMaxFilledSlots,
                actualFilledSlotsAfterInitial,
                bestValidFilledSlots,
                List.copyOf(bestValidWarningCodes),
                planningInputChars,
                bestValidPromptChars,
                bestValidPreview.responseChars(),
                maxCompletionTokens
        );
    }

    private void validateRoundStructure(
            CreateFreeGameAssignmentPreviewCommand command,
            AssignmentPreviewPromptPayload promptPayload,
            AssignmentPreviewAiRawResponse response
    ) {
        if (command == null) {
            return;
        }

        validateResponseShapeNotNull(response);
        validateWarningsShape(response);
        validateParticipantIds(promptPayload, response);

        if (command.rounds().size() != response.rounds().size()) {
            throw invalidOutput();
        }

        for (int i = 0; i < command.rounds().size(); i++) {
            CreateFreeGameAssignmentPreviewCommand.Round requestedRound =
                    command.rounds().get(i);
            AssignmentPreviewAiRawResponse.Round responseRound =
                    response.rounds().get(i);

            validateRound(requestedRound, responseRound);

            if (command.preferences().existingAssignmentPolicy()
                    == CreateFreeGameAssignmentPreviewCommand.ExistingAssignmentPolicy.FILL_EMPTY_SLOTS) {
                validateFixedSlots(requestedRound, responseRound, promptPayload);
            }
        }

        validateRoundLayoutVariation(response);
    }

    private void validateRound(
            CreateFreeGameAssignmentPreviewCommand.Round requestedRound,
            AssignmentPreviewAiRawResponse.Round responseRound
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
            AssignmentPreviewAiRawResponse.Round responseRound
    ) {
        for (int j = 0; j < requestedRound.courts().size(); j++) {
            CreateFreeGameAssignmentPreviewCommand.Court requestedCourt =
                    requestedRound.courts().get(j);
            AssignmentPreviewAiRawResponse.Court responseCourt =
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
            AssignmentPreviewAiRawResponse.Round responseRound,
            AssignmentPreviewPromptPayload promptPayload
    ) {
        for (int i = 0; i < requestedRound.courts().size(); i++) {
            CreateFreeGameAssignmentPreviewCommand.Court requestedCourt =
                    requestedRound.courts().get(i);
            AssignmentPreviewAiRawResponse.Court responseCourt =
                    responseRound.courts().get(i);

            List<String> requestedSlots = requestedCourt.slots();
            List<Long> responseSlots = responseCourt.slots();

            for (int j = 0; j < requestedSlots.size(); j++) {
                String requestedSlot = requestedSlots.get(j);
                Long responseSlot = responseSlots.get(j);

                if (requestedSlot != null && !compactIdEqualsRequested(promptPayload, requestedSlot, responseSlot)) {
                    throw invalidOutput();
                }
            }
        }
    }

    private void validateParticipantIds(
            AssignmentPreviewPromptPayload promptPayload,
            AssignmentPreviewAiRawResponse response
    ) {
        Set<Long> participantIds = promptPayload.compactParticipantIds();

        for (AssignmentPreviewAiRawResponse.Round round : response.rounds()) {
            for (AssignmentPreviewAiRawResponse.Court court : round.courts()) {
                for (Long slot : court.slots()) {
                    if (slot != null && !participantIds.contains(slot)) {
                        throw invalidOutput();
                    }
                }
            }
        }
    }

    private void validateDuplicateParticipantsInRound(AssignmentPreviewAiRawResponse.Round responseRound) {
        Set<Long> assignedParticipants = new HashSet<>();

        for (AssignmentPreviewAiRawResponse.Court court : responseRound.courts()) {
            for (Long slot : court.slots()) {
                if (slot != null && !assignedParticipants.add(slot)) {
                    throw invalidOutput();
                }
            }
        }
    }

    private RequestedPreview requestAssignmentPreview(
            String promptText,
            OpenAiChatOptions options
    ) {
        ChatResponse response;
        try {
            response = chatModel.call(new Prompt(promptText, options));
        } catch (ResourceAccessException ex) {
            if (isTimeoutException(ex)) {
                throw new ServiceUnavailableException(OPENAI_TIMEOUT_MESSAGE);
            }
            throw new ServiceUnavailableException("AI 코트 배정 프리뷰를 현재 생성할 수 없습니다.", ex);
        }

        if (response.getResult() == null || response.getResult().getOutput() == null) {
            throw new EmptyResponseException();
        }

        String responseText = response.getResult().getOutput().getText();
        if (responseText == null || responseText.isBlank()) {
            throw new EmptyResponseException();
        }

        try {
            return new RequestedPreview(
                    objectMapper.readValue(responseText, AssignmentPreviewAiRawResponse.class),
                    responseText.length()
            );
        } catch (JacksonException ex) {
            throw new IllegalStateException("OpenAI로부터 응답을 읽을 수 없습니다.", ex);
        }
    }

    private String buildRepairPrompt(
            String planningInputJson,
            String failureReason
    ) {
        return ASSIGNMENT_PREVIEW_REPAIR_PROMPT.formatted(resolveRepairFailureReason(failureReason))
                + planningInputJson;
    }

    private String buildEmptyResponseRetryPrompt(String promptText) {
        return promptText + EMPTY_RESPONSE_RETRY_PROMPT_SUFFIX;
    }

    private String buildQualityRepairPrompt(
            String planningInputJson,
            AssignmentPreviewAiRawResponse previousResponse,
            QualityEvaluation qualityEvaluation,
            int attemptNumber
    ) {
        String attemptIntro = attemptNumber > 1
                ? "A previous quality repair did not fully resolve the issues. Improve further."
                : "";
        String issues = qualityEvaluation.deficiencies().stream()
                .map(deficiency -> switch (deficiency) {
                    case UNDER_FILLED -> "The previous output is under-filled. It filled %d slots out of a theoretical maximum of %d."
                            .formatted(
                                    qualityEvaluation.filledSlots(),
                                    qualityEvaluation.theoreticalMaxFilledSlots()
                            );
                    case INCONSISTENT_WARNINGS -> "The warnings do not match the final fill coverage. Keep fill coverage unchanged if it is already maximal and make the warnings consistent.";
                })
                .collect(Collectors.joining("\n"));
        return ASSIGNMENT_PREVIEW_QUALITY_REPAIR_PROMPT.formatted(
                attemptIntro,
                issues,
                planningInputJson,
                serializeResponse(previousResponse)
        );
    }

    private String serializePlanningInput(AssignmentPreviewPromptPayload promptPayload)
            throws JacksonException {
        if (promptPayload == null) {
            return objectMapper.writeValueAsString(null);
        }

        return objectMapper.writeValueAsString(promptPayload.planningInput());
    }

    private String serializeResponse(AssignmentPreviewAiRawResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JacksonException ex) {
            throw new IllegalStateException("OpenAI로부터 응답을 읽을 수 없습니다.", ex);
        }
    }

    private void validateResponseShapeNotNull(AssignmentPreviewAiRawResponse response) {
        if (response.rounds() == null) {
            throw invalidOutput();
        }

        for (AssignmentPreviewAiRawResponse.Round round : response.rounds()) {
            if (round == null || round.roundNumber() == null || round.courts() == null) {
                throw invalidOutput();
            }

            for (AssignmentPreviewAiRawResponse.Court court : round.courts()) {
                if (court == null || court.courtNumber() == null || court.slots() == null) {
                    throw invalidOutput();
                }
            }
        }
    }

    private void validateWarningsShape(AssignmentPreviewAiRawResponse response) {
        if (response.warnings() == null) {
            throw invalidOutput();
        }

        for (AssignmentPreviewAiRawResponse.Warning warning : response.warnings()) {
            if (warning == null || warning.code() == null || warning.message() == null) {
                throw invalidOutput();
            }
        }
    }

    private void validateRoundLayoutVariation(AssignmentPreviewAiRawResponse response) {
        Set<RoundLayoutSignature> seenRoundLayouts = new HashSet<>();

        for (AssignmentPreviewAiRawResponse.Round round : response.rounds()) {
            if (!seenRoundLayouts.add(RoundLayoutSignature.from(round))) {
                throw repeatedRoundLayout();
            }
        }
    }

    private boolean isRepairableInvalidOutput(IllegalStateException ex) {
        return INVALID_OUTPUT_MESSAGE.equals(ex.getMessage())
                || REPEATED_ROUND_LAYOUT_MESSAGE.equals(ex.getMessage());
    }

    private String resolveRepairFailureReason(String failureReason) {
        if (REPEATED_ROUND_LAYOUT_MESSAGE.equals(failureReason)) {
            return "The previous output copied the same round layout across multiple rounds.";
        }
        return "The previous output did not satisfy the required structure or constraints.";
    }

    private AssignmentPreviewAiResponse remapResponse(
            AssignmentPreviewAiRawResponse rawResponse,
            AssignmentPreviewPromptPayload promptPayload
    ) {
        List<AssignmentPreviewAiRawResponse.Warning> normalizedWarnings = normalizeWarnings(rawResponse);
        return new AssignmentPreviewAiResponse(
                rawResponse.rounds().stream()
                        .map(round -> new AssignmentPreviewAiResponse.Round(
                                round.roundNumber(),
                                round.courts().stream()
                                        .map(court -> new AssignmentPreviewAiResponse.Court(
                                                court.courtNumber(),
                                                court.slots().stream()
                                                        .map(slot -> slot == null ? null : promptPayload.toClientId(slot))
                                                        .toList()
                                        ))
                                        .toList()
                        ))
                        .toList(),
                normalizedWarnings.stream()
                        .map(warning -> new AssignmentPreviewAiResponse.Warning(
                                warning.code(),
                                WARNING_MESSAGE_BY_CODE.getOrDefault(
                                        warning.code(),
                                        warning.message()
                                )
                        ))
                        .toList()
        );
    }

    private List<AssignmentPreviewAiRawResponse.Warning> normalizeWarnings(AssignmentPreviewAiRawResponse rawResponse) {
        boolean hasRemainingEmptySlot = rawResponse.rounds().stream()
                .flatMap(round -> round.courts().stream())
                .flatMap(court -> court.slots().stream())
                .anyMatch(slot -> slot == null);

        if (hasRemainingEmptySlot) {
            return rawResponse.warnings();
        }

        return rawResponse.warnings().stream()
                .filter(warning -> !"PARTIAL_ASSIGNMENT".equals(warning.code()))
                .filter(warning -> !"NO_FURTHER_IMPROVEMENT".equals(warning.code()))
                .toList();
    }

    private QualityEvaluation evaluateQuality(
            CreateFreeGameAssignmentPreviewCommand command,
            AssignmentPreviewAiRawResponse response,
            Integer theoreticalMaxFilledSlots
    ) {
        int filledSlots = countFilledSlots(response);
        List<String> normalizedWarningCodes = normalizeWarnings(response).stream()
                .map(AssignmentPreviewAiRawResponse.Warning::code)
                .toList();
        boolean hasRemainingEmptySlots = response.rounds().stream()
                .flatMap(round -> round.courts().stream())
                .flatMap(court -> court.slots().stream())
                .anyMatch(slot -> slot == null);
        List<QualityDeficiency> deficiencies = new ArrayList<>();

        if (command != null
                && command.preferences().existingAssignmentPolicy()
                == CreateFreeGameAssignmentPreviewCommand.ExistingAssignmentPolicy.FILL_EMPTY_SLOTS
                && theoreticalMaxFilledSlots != null
                && filledSlots < theoreticalMaxFilledSlots) {
            deficiencies.add(QualityDeficiency.UNDER_FILLED);
        }

        if (hasInconsistentWarnings(hasRemainingEmptySlots, normalizedWarningCodes)) {
            deficiencies.add(QualityDeficiency.INCONSISTENT_WARNINGS);
        }

        return new QualityEvaluation(
                filledSlots,
                theoreticalMaxFilledSlots == null ? filledSlots : theoreticalMaxFilledSlots,
                normalizedWarningCodes,
                List.copyOf(deficiencies)
        );
    }

    private boolean hasInconsistentWarnings(boolean hasRemainingEmptySlots, List<String> warningCodes) {
        boolean hasPartialCoverageWarning = warningCodes.contains("PARTIAL_ASSIGNMENT")
                || warningCodes.contains("NO_FURTHER_IMPROVEMENT");
        if (hasRemainingEmptySlots) {
            return !hasPartialCoverageWarning;
        }
        return hasPartialCoverageWarning;
    }

    private boolean isBetterCandidate(QualityEvaluation currentBest, QualityEvaluation candidate) {
        if (candidate.filledSlots() != currentBest.filledSlots()) {
            return candidate.filledSlots() > currentBest.filledSlots();
        }
        if (candidate.hasWarningInconsistency() != currentBest.hasWarningInconsistency()) {
            return !candidate.hasWarningInconsistency();
        }
        if (candidate.normalizedWarningCodes().size() != currentBest.normalizedWarningCodes().size()) {
            return candidate.normalizedWarningCodes().size() < currentBest.normalizedWarningCodes().size();
        }
        return false;
    }

    private void mergeQualityRepairReasons(
            List<String> target,
            List<String> additions
    ) {
        for (String addition : additions) {
            if (!target.contains(addition)) {
                target.add(addition);
            }
        }
    }

    private boolean compactIdEqualsRequested(
            AssignmentPreviewPromptPayload promptPayload,
            String requestedSlot,
            Long responseSlot
    ) {
        if (responseSlot == null) {
            return false;
        }
        Long expectedCompactId = promptPayload.toCompactId(requestedSlot);
        return expectedCompactId != null && expectedCompactId.equals(responseSlot);
    }

    private IllegalStateException invalidOutput() {
        return new IllegalStateException(INVALID_OUTPUT_MESSAGE);
    }

    private IllegalStateException repeatedRoundLayout() {
        return new IllegalStateException(REPEATED_ROUND_LAYOUT_MESSAGE);
    }

    private Integer theoreticalMaxFilledSlots(CreateFreeGameAssignmentPreviewCommand command) {
        if (command == null) {
            return null;
        }

        int participantCount = command.participants().size();
        int total = 0;
        for (CreateFreeGameAssignmentPreviewCommand.Round round : command.rounds()) {
            int requestedFilled = 0;
            int requestedNull = 0;
            for (CreateFreeGameAssignmentPreviewCommand.Court court : round.courts()) {
                for (String slot : court.slots()) {
                    if (slot == null) {
                        requestedNull++;
                    } else {
                        requestedFilled++;
                    }
                }
            }
            total += requestedFilled + Math.min(requestedNull, Math.max(participantCount - requestedFilled, 0));
        }
        return total;
    }

    private int countFilledSlots(AssignmentPreviewAiRawResponse response) {
        return response.rounds().stream()
                .flatMap(round -> round.courts().stream())
                .flatMap(court -> court.slots().stream())
                .mapToInt(slot -> slot == null ? 0 : 1)
                .sum();
    }

    private AssignmentPreviewAiInvalidResponseException invalidResponse(
            String message,
            Throwable cause,
            String model,
            boolean repairAttempted,
            Long initialAiElapsedMs,
            Long repairAiElapsedMs,
            boolean emptyResponseRetryAttempted,
            Long emptyResponseRetryElapsedMs,
            Integer qualityRepairAttemptCount,
            Long qualityRepairElapsedMsTotal,
            List<String> qualityRepairReasons,
            Integer theoreticalMaxFilledSlots,
            Integer actualFilledSlotsAfterInitial,
            Integer bestValidFilledSlots,
            List<String> bestValidWarningCodes,
            Integer planningInputChars,
            Integer promptChars,
            Integer responseChars,
            Integer maxCompletionTokens
    ) {
        return new AssignmentPreviewAiInvalidResponseException(
                message,
                cause,
                model,
                repairAttempted,
                initialAiElapsedMs,
                repairAiElapsedMs,
                emptyResponseRetryAttempted,
                emptyResponseRetryElapsedMs,
                qualityRepairAttemptCount,
                qualityRepairElapsedMsTotal,
                qualityRepairReasons,
                theoreticalMaxFilledSlots,
                actualFilledSlotsAfterInitial,
                bestValidFilledSlots,
                bestValidWarningCodes,
                planningInputChars,
                promptChars,
                responseChars,
                maxCompletionTokens
        );
    }

    private RuntimeException wrapFailure(
            RuntimeException ex,
            String model,
            boolean repairAttempted,
            Long initialAiElapsedMs,
            Long repairAiElapsedMs,
            boolean emptyResponseRetryAttempted,
            Long emptyResponseRetryElapsedMs,
            Integer qualityRepairAttemptCount,
            Long qualityRepairElapsedMsTotal,
            List<String> qualityRepairReasons,
            Integer theoreticalMaxFilledSlots,
            Integer actualFilledSlotsAfterInitial,
            Integer bestValidFilledSlots,
            List<String> bestValidWarningCodes,
            Integer planningInputChars,
            Integer promptChars,
            Integer responseChars,
            Integer maxCompletionTokens
    ) {
        if (ex instanceof AssignmentPreviewAiExecutionFailure) {
            return ex;
        }
        if (ex instanceof ServiceUnavailableException serviceUnavailableException) {
            return new AssignmentPreviewAiServiceUnavailableException(
                    serviceUnavailableException.getMessage(),
                    serviceUnavailableException.getCause(),
                    resolveFailureCode(serviceUnavailableException),
                    model,
                    repairAttempted,
                    initialAiElapsedMs,
                    repairAiElapsedMs,
                    emptyResponseRetryAttempted,
                    emptyResponseRetryElapsedMs,
                    qualityRepairAttemptCount,
                    qualityRepairElapsedMsTotal,
                    qualityRepairReasons,
                    theoreticalMaxFilledSlots,
                    actualFilledSlotsAfterInitial,
                    bestValidFilledSlots,
                    bestValidWarningCodes,
                    planningInputChars,
                    promptChars,
                    responseChars,
                    maxCompletionTokens
            );
        }
        if (ex instanceof IllegalStateException illegalStateException) {
            return invalidResponse(
                    illegalStateException.getMessage(),
                    illegalStateException.getCause(),
                    model,
                    repairAttempted,
                    initialAiElapsedMs,
                    repairAiElapsedMs,
                    emptyResponseRetryAttempted,
                    emptyResponseRetryElapsedMs,
                    qualityRepairAttemptCount,
                    qualityRepairElapsedMsTotal,
                    qualityRepairReasons,
                    theoreticalMaxFilledSlots,
                    actualFilledSlotsAfterInitial,
                    bestValidFilledSlots,
                    bestValidWarningCodes,
                    planningInputChars,
                    promptChars,
                    responseChars,
                    maxCompletionTokens
            );
        }
        return ex;
    }

    private AssignmentPreviewJobFailureCode resolveFailureCode(ServiceUnavailableException ex) {
        if (OPENAI_TIMEOUT_MESSAGE.equals(ex.getMessage())) {
            return AssignmentPreviewJobFailureCode.TIMEOUT;
        }
        return AssignmentPreviewJobFailureCode.SERVICE_UNAVAILABLE;
    }

    private long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000L;
    }

    private void applySamplingOptions(OpenAiChatOptions options, String model) {
        if (supportsCustomTemperature(model)) {
            options.setTemperature(PREVIEW_TEMPERATURE);
        }
    }

    private boolean supportsCustomTemperature(String model) {
        if (model == null || model.isBlank()) {
            return true;
        }

        String normalizedModel = model.trim().toLowerCase();
        return !normalizedModel.startsWith("gpt-5")
                || normalizedModel.startsWith("gpt-5-chat");
    }

    private boolean isTimeoutException(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof SocketTimeoutException
                    || current instanceof HttpTimeoutException
                    || current instanceof TimeoutException
                    || current instanceof ConnectTimeoutException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    @Getter
    private static final class RoundLayoutSignature {

        private final List<CourtLayoutSignature> courts;

        private RoundLayoutSignature(List<CourtLayoutSignature> courts) {
            this.courts = List.copyOf(courts);
        }

        static RoundLayoutSignature from(AssignmentPreviewAiRawResponse.Round round) {
            List<CourtLayoutSignature> courts = new ArrayList<>();
            for (AssignmentPreviewAiRawResponse.Court court : round.courts()) {
                courts.add(new CourtLayoutSignature(court.courtNumber(), court.slots()));
            }
            return new RoundLayoutSignature(courts);
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof RoundLayoutSignature that)) {
                return false;
            }
            return courts.equals(that.courts);
        }

        @Override
        public int hashCode() {
            return courts.hashCode();
        }
    }

    private record RequestedPreview(
            AssignmentPreviewAiRawResponse response,
            int responseChars
    ) {
    }

    private record QualityEvaluation(
            int filledSlots,
            int theoreticalMaxFilledSlots,
            List<String> normalizedWarningCodes,
            List<QualityDeficiency> deficiencies
    ) {

        private boolean hasDeficiencies() {
            return !deficiencies.isEmpty();
        }

        private boolean hasWarningInconsistency() {
            return deficiencies.contains(QualityDeficiency.INCONSISTENT_WARNINGS);
        }

        private List<String> deficiencyNames() {
            return deficiencies.stream()
                    .map(Enum::name)
                    .toList();
        }
    }

    private enum QualityDeficiency {
        UNDER_FILLED,
        INCONSISTENT_WARNINGS
    }

    private static final class EmptyResponseException extends IllegalStateException {

        private EmptyResponseException() {
            super(EMPTY_RESPONSE_MESSAGE);
        }
    }

    private record CourtLayoutSignature(
            Integer courtNumber,
            List<Long> slots
    ) {

        private CourtLayoutSignature(Integer courtNumber, List<Long> slots) {
            this.courtNumber = courtNumber;
            this.slots = new ArrayList<>(slots);
        }
    }

}
