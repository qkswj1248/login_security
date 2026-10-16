package com.banban.login_security.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthorizationLogErrorCode implements Code {
    LOG_NOT_FOUND("LOG_1", "에이전트 로그를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    INVALID_LOG("LOG_2", "로그의 자원, 행동, 범위, 결과 또는 사유가 올바르지 않습니다", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
