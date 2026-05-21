package com.gumraze.rallyon.backend.courtManager.application.port.in;

import com.gumraze.rallyon.backend.courtManager.application.port.in.command.CompleteFreeGameMatchCommand;

/**
 * 자유게임 하위 매치 종료 유스케이스다.
 */
public interface CompleteFreeGameMatchUseCase {

    /**
     * 특정 라운드/코트의 진행 중 매치를 기록 방식에 맞게 완료한다.
     *
     * @param command 운영자, 매치 식별자, 종료 기록 정보
     */
    void complete(CompleteFreeGameMatchCommand command);
}
