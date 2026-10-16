package com.banban.login_security.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AgentPermissionErrorCode implements Code {
    PERMISSION_NOT_FOUND("PERMISSION_1", "권한 요청 기록을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    INVALID_PERMISSION("PERMISSION_2", "resource와 action은 1~255자, scope는 최대 255자여야 합니다", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
