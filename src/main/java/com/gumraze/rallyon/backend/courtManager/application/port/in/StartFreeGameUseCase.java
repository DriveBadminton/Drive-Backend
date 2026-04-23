package com.gumraze.rallyon.backend.courtManager.application.port.in;

import com.gumraze.rallyon.backend.courtManager.application.port.in.command.StartFreeGameCommand;
import com.gumraze.rallyon.backend.courtManager.dto.UpdateFreeGameResponse;

/**
 * 자유게임 운영 시작 유스케이스다.
 */
public interface StartFreeGameUseCase {

    /**
     * 미진행 자유게임을 진행 중 상태로 전환한다.
     *
     * @param command 운영자와 자유게임 식별자
     * @return 시작된 자유게임 식별자
     */
    UpdateFreeGameResponse start(StartFreeGameCommand command);
}
