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
import org.springframework.beans.factory.annotation.Value;
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
    private static final String OPENAI_TIMEOUT_MESSAGE =
            "AI 자동 배정 응답 시간이 초과되었습니다. 잠시 후 다시 시도해주세요.";
    private static final Double PREVIEW_TEMPERATURE = 0.0d;
    private static final Integer PREVIEW_MAX_COMPLETION_TOKENS = 1200;
    private static final String ASSIGNMENT_PREVIEW_PROMPT = """
            자유게임 코트 배정 preview JSON을 생성하세요.
            규칙:
            - 입력과 동일한 rounds/courts 구조 유지
            - 같은 라운드 중복 참가자 금지
            - 이전 라운드 전체 복제 금지
            - 각 라운드는 가능한 범위에서 다른 조합 사용
            - 동일한 court-level 4인 배치 반복 금지
            - guidance.preserveFixedSlots=true 이면 fixed=true 슬롯 유지
            - guidance.fillEmptySlotsOnly=true 이면 기존 non-null 유지 후 null만 최대한 채우기
            - guidance.preferProvidedPartnerPairs=true 이면 partnerPairs 우선
            - 빈 슬롯이 남으면 PARTIAL_ASSIGNMENT 추가
            - 파트너 선호 일부 미충족이면 PARTNER_CONSTRAINT_PARTIAL 추가
            - 개선 불가여도 warnings 비우지 않기
            - rounds와 warnings만 포함한 JSON만 반환
            입력:
            """;

    private static final String ASSIGNMENT_PREVIEW_REPAIR_PROMPT = """
            이전 응답은 요청 구조와 제약을 만족하지 못했습니다.
            실패 사유: %s
            다시 생성하세요.
            규칙:
            - 입력과 동일한 rounds/courts 구조 유지
            - 같은 라운드 중복 참가자 금지
            - 이전 라운드 전체 복제 금지
            - 각 라운드는 가능한 범위에서 다른 조합 사용
            - 동일한 court-level 4인 배치 반복 금지
            - guidance.preserveFixedSlots=true 이면 fixed=true 슬롯 유지
            - guidance.fillEmptySlotsOnly=true 이면 기존 non-null 유지 후 null만 최대한 채우기
            - guidance.preferProvidedPartnerPairs=true 이면 partnerPairs 우선
            - 빈 슬롯이 남으면 PARTIAL_ASSIGNMENT 추가
            - 파트너 선호 일부 미충족이면 PARTNER_CONSTRAINT_PARTIAL 추가
            - 개선 불가여도 warnings 비우지 않기
            - rounds와 warnings만 포함한 JSON만 반환
            입력:
            """;

    private final AssignmentPreviewPlanningInputMapper planningInputMapper;
    private final OpenAiChatModel chatModel;
    private final ObjectMapper objectMapper;

    @Value("${spring.ai.openai.chat.options.model:gpt-5-mini}")
    private String configuredModel = "gpt-5-mini";

    public SpringAiAssignmentPreviewGateway(
            AssignmentPreviewPlanningInputMapper planningInputMapper,
            @Qualifier("assignmentPreviewChatModel") OpenAiChatModel chatModel,
            ObjectMapper objectMapper
    ) {
        this.planningInputMapper = planningInputMapper;
        this.chatModel = chatModel;
        this.objectMapper = objectMapper;
    }

    @Override
    public AssignmentPreviewAiGenerationResult generateExecution(
            CreateFreeGameAssignmentPreviewCommand command
    ) {
        Map<String, Object> schema;
        String planningInputJson;
        String promptText;
        String model = configuredModel;
        int planningInputChars;
        int promptChars;
        try {
            schema = objectMapper.readValue(
                    AssignmentPreviewAiSchema.ASSIGNMENT_PREVIEW_JSON_SCHEMA,
                    new TypeReference<Map<String, Object>>() {
                    }
            );
            planningInputJson = serializePlanningInput(command);
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
                    null,
                    null,
                    null,
                    PREVIEW_MAX_COMPLETION_TOKENS
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
        options.setTemperature(PREVIEW_TEMPERATURE);
        options.setMaxCompletionTokens(PREVIEW_MAX_COMPLETION_TOKENS);

        Long initialAiElapsedMs = null;
        Long repairAiElapsedMs = null;

        RequestedPreview aiResponse;
        long initialStartNanos = System.nanoTime();
        try {
            aiResponse = requestAssignmentPreview(promptText, options);
        } catch (RuntimeException ex) {
            initialAiElapsedMs = elapsedMillis(initialStartNanos);
            throw wrapFailure(
                    ex,
                    model,
                    false,
                    initialAiElapsedMs,
                    null,
                    planningInputChars,
                    promptChars,
                    null,
                    PREVIEW_MAX_COMPLETION_TOKENS
            );
        }
        initialAiElapsedMs = elapsedMillis(initialStartNanos);

        try {
            validateRoundStructure(command, aiResponse.response());
            return new AssignmentPreviewAiGenerationResult(
                    aiResponse.response(),
                    model,
                    false,
                    initialAiElapsedMs,
                    null,
                    planningInputChars,
                    promptChars,
                    aiResponse.responseChars(),
                    PREVIEW_MAX_COMPLETION_TOKENS
            );
        } catch (IllegalStateException ex) {
            if (!isRepairableInvalidOutput(ex)) {
                throw wrapFailure(
                        ex,
                        model,
                        false,
                        initialAiElapsedMs,
                        null,
                        planningInputChars,
                        promptChars,
                        aiResponse.responseChars(),
                        PREVIEW_MAX_COMPLETION_TOKENS
                );
            }
            String repairPrompt = buildRepairPrompt(planningInputJson, ex.getMessage());
            int repairPromptChars = repairPrompt.length();
            RequestedPreview repairedResponse;
            long repairStartNanos = System.nanoTime();
            try {
                repairedResponse = requestAssignmentPreview(repairPrompt, options);
            } catch (RuntimeException retryEx) {
                repairAiElapsedMs = elapsedMillis(repairStartNanos);
                throw wrapFailure(
                        retryEx,
                        model,
                        true,
                        initialAiElapsedMs,
                        repairAiElapsedMs,
                        planningInputChars,
                        repairPromptChars,
                        null,
                        PREVIEW_MAX_COMPLETION_TOKENS
                );
            }
            repairAiElapsedMs = elapsedMillis(repairStartNanos);
            try {
                validateRoundStructure(command, repairedResponse.response());
            } catch (IllegalStateException retryValidationEx) {
                throw wrapFailure(
                        retryValidationEx,
                        model,
                        true,
                        initialAiElapsedMs,
                        repairAiElapsedMs,
                        planningInputChars,
                        repairPromptChars,
                        repairedResponse.responseChars(),
                        PREVIEW_MAX_COMPLETION_TOKENS
                );
            }
            return new AssignmentPreviewAiGenerationResult(
                    repairedResponse.response(),
                    model,
                    true,
                    initialAiElapsedMs,
                    repairAiElapsedMs,
                    planningInputChars,
                    repairPromptChars,
                    repairedResponse.responseChars(),
                    PREVIEW_MAX_COMPLETION_TOKENS
            );
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

        validateRoundLayoutVariation(response);
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
            throw new IllegalStateException("OpenAI 응답이 비어 있습니다.");
        }

        String responseText = response.getResult().getOutput().getText();
        if (responseText == null || responseText.isBlank()) {
            throw new IllegalStateException("OpenAI 응답이 비어 있습니다.");
        }

        try {
            return new RequestedPreview(
                    objectMapper.readValue(responseText, AssignmentPreviewAiResponse.class),
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

    private void validateRoundLayoutVariation(AssignmentPreviewAiResponse response) {
        Set<RoundLayoutSignature> seenRoundLayouts = new HashSet<>();

        for (AssignmentPreviewAiResponse.Round round : response.rounds()) {
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
        return "요청 구조 또는 제약 조건을 만족하지 못했습니다.";
    }

    private IllegalStateException invalidOutput() {
        return new IllegalStateException(INVALID_OUTPUT_MESSAGE);
    }

    private IllegalStateException repeatedRoundLayout() {
        return new IllegalStateException(REPEATED_ROUND_LAYOUT_MESSAGE);
    }

    private AssignmentPreviewAiInvalidResponseException invalidResponse(
            String message,
            Throwable cause,
            String model,
            boolean repairAttempted,
            Long initialAiElapsedMs,
            Long repairAiElapsedMs,
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

        static RoundLayoutSignature from(AssignmentPreviewAiResponse.Round round) {
            List<CourtLayoutSignature> courts = new ArrayList<>();
            for (AssignmentPreviewAiResponse.Court court : round.courts()) {
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
            AssignmentPreviewAiResponse response,
            int responseChars
    ) {
    }

    private record CourtLayoutSignature(
            Integer courtNumber,
            List<String> slots
    ) {

        private CourtLayoutSignature(Integer courtNumber, List<String> slots) {
            this.courtNumber = courtNumber;
            this.slots = new ArrayList<>(slots);
        }
    }

}
