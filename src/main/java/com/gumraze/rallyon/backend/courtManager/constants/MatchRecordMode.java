package com.gumraze.rallyon.backend.courtManager.constants;

public enum MatchRecordMode {
    STATUS_ONLY,    // 매치 시작/종료 상태만 저장
    WINNER_ONLY,    // 점수 없이 승자만 저장
    SCORE           // 점수와 승자를 저장
}
