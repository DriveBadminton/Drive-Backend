package com.gumraze.rallyon.backend.courtManager.adapter.out.ai;

import java.util.List;

/**
 * OpenAI structured output schema와 직접 맞물리는 내부 응답 구조다.
 *
 * <p>participant 식별자는 토큰 절감을 위해 compact numeric id를 사용한다.
 */
public record AssignmentPreviewAiRawResponse(
        List<Round> rounds,
        List<Warning> warnings
) {

    public record Round(
            Integer roundNumber,
            List<Court> courts
    ) {
    }

    public record Court(
            Integer courtNumber,
            List<Long> slots
    ) {
    }

    public record Warning(
            String code,
            String message
    ) {
    }
}
