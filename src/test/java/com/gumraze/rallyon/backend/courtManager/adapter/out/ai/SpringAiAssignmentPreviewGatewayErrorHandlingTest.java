package com.gumraze.rallyon.backend.courtManager.adapter.out.ai;

import com.gumraze.rallyon.backend.common.exception.ServiceUnavailableException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.web.client.ResourceAccessException;

import java.net.SocketTimeoutException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class SpringAiAssignmentPreviewGatewayErrorHandlingTest extends SpringAiAssignmentPreviewGatewayTestSupport {

    @Test
    @DisplayName("OpenAI 호출 timeout이면 service unavailable로 변환한다")
    void generate_whenOpenAiCallTimesOut_throwsServiceUnavailableException() {
        given(chatModel.call(any(Prompt.class)))
                .willThrow(new ResourceAccessException(
                        "I/O error",
                        new SocketTimeoutException("Read timed out")
                ));

        assertThatThrownBy(() -> getGateway().generate(getSingleRoundCommand()))
                .isInstanceOf(ServiceUnavailableException.class)
                .hasMessage("AI 자동 배정 응답 시간이 초과되었습니다. 잠시 후 다시 시도해주세요.");
    }
}
