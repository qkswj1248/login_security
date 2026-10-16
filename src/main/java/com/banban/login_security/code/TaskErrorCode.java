package com.banban.login_security.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TaskErrorCode implements Code {
    TASK_NOT_FOUND("TASK_1", "작업을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    INVALID_TASK("TASK_2", "작업 목적(1~500자)과 미래의 만료 시각이 필요합니다", HttpStatus.BAD_REQUEST),
    INVALID_TASK_STATUS("TASK_3", "변경할 상태는 COMPLETED 또는 CANCELLED여야 합니다", HttpStatus.BAD_REQUEST),
    TASK_NOT_RUNNING("TASK_4", "종료되거나 만료된 작업은 변경할 수 없습니다", HttpStatus.CONFLICT),
    AGENT_NOT_ACTIVE("TASK_5", "활성 상태인 에이전트만 작업을 시작할 수 있습니다", HttpStatus.CONFLICT);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
