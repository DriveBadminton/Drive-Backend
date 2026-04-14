package com.gumraze.rallyon.backend.courtManager.application.port.out.result;

import com.gumraze.rallyon.backend.courtManager.application.port.in.result.CreateFreeGameAssignmentPreviewResult;

/**
 * AI 자동 배정 preview 생성 결과와 실행 메타데이터다.
 *
 * @param preview 라운드별 preview 결과
 * @param model 호출에 사용된 모델명
 * @param repairAttempted invalid output 보정 재시도 수행 여부
 * @param initialAiElapsedMs 최초 AI 호출 elapsed
 * @param repairAiElapsedMs 보정 AI 호출 elapsed
 * @param planningInputChars planning input JSON 문자 수
 * @param promptChars 최종 시도 prompt 문자 수
 * @param responseChars 성공 응답 문자 수
 * @param maxCompletionTokens 요청에 사용한 최대 completion token
 */
public record FreeGameAssignmentPreviewGeneration(
        CreateFreeGameAssignmentPreviewResult preview,
        String model,
        boolean repairAttempted,
        Long initialAiElapsedMs,
        Long repairAiElapsedMs,
        Integer planningInputChars,
        Integer promptChars,
        Integer responseChars,
        Integer maxCompletionTokens
) {
}
