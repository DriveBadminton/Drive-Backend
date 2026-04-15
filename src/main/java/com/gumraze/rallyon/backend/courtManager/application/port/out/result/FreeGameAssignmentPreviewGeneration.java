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
 * @param emptyResponseRetryAttempted 빈 응답 1회 follow-up 수행 여부
 * @param emptyResponseRetryElapsedMs 빈 응답 follow-up 호출 elapsed
 * @param qualityRepairAttemptCount 구조 valid 이후 품질 개선 재시도 횟수
 * @param qualityRepairElapsedMsTotal 품질 개선 재시도 누적 elapsed
 * @param qualityRepairReasons 품질 개선을 유발한 deficiency reason 목록
 * @param theoreticalMaxFilledSlots 요청 기준 이론적 최대 filled slot 수
 * @param actualFilledSlotsAfterInitial 최초 구조 valid 응답의 실제 filled slot 수
 * @param bestValidFilledSlots 최종 선택된 best valid 응답의 filled slot 수
 * @param bestValidWarningCodes 최종 선택된 best valid 응답의 warning code 목록
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
        boolean emptyResponseRetryAttempted,
        Long emptyResponseRetryElapsedMs,
        Integer qualityRepairAttemptCount,
        Long qualityRepairElapsedMsTotal,
        java.util.List<String> qualityRepairReasons,
        Integer theoreticalMaxFilledSlots,
        Integer actualFilledSlotsAfterInitial,
        Integer bestValidFilledSlots,
        java.util.List<String> bestValidWarningCodes,
        Integer planningInputChars,
        Integer promptChars,
        Integer responseChars,
        Integer maxCompletionTokens
) {
}
