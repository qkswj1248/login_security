package com.banban.login_security.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AgentErrorCode implements Code {
    AGENT_NOT_FOUND("AGENT_1", "에이전트를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    INVALID_AGENT_NAME("AGENT_2", "이름은 1~255자여야 합니다", HttpStatus.BAD_REQUEST),
    INVALID_AGENT_STATUS("AGENT_3", "상태는 ACTIVE 또는 DISABLED여야 합니다", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
