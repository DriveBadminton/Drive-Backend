package com.gumraze.rallyon.backend.courtManager.application.port.in;

import com.gumraze.rallyon.backend.courtManager.application.port.in.command.StartFreeGameMatchCommand;

/**
 * 자유게임 하위 매치 시작 유스케이스다.
 */
public interface StartFreeGameMatchUseCase {

    /**
     * 특정 라운드/코트의 미진행 매치를 진행 중 상태로 전환한다.
     *
     * @param command 운영자, 자유게임, 라운드, 코트 식별자
     */
    void start(StartFreeGameMatchCommand command);
}
