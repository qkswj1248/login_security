package com.banban.login_security.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements Code{
    // 잘못된 매개변수 포함
    INVALID_PARAMETER("", "잘못된 매개변수 포함"),
    // 리소스 없음
    RESOURCE_NOT_FOUND("", "리소스 없음"),
    // 내부 서버 오류
    INTERNAL_SERVER_ERROR("", "내부 서버 오류"),
    SQL_ERROR("", "SQL 오류"),
    DATA_ACCESS_ERROR("SERVICE_1", "sql 접근 에러")
    ;
    private final String code;
    private final String message;
    private final HttpStatus httpStatus = HttpStatus.NOT_FOUND;

}
