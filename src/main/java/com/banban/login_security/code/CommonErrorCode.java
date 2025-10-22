package com.banban.login_security.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements Code{
    // 잘못된 매개변수 포함
    INVALID_PARAMETER(404, "잘못된 매개변수 포함", HttpStatus.NOT_FOUND),
    // 리소스 없음
    RESOURCE_NOT_FOUND(404, "리소스 없음", HttpStatus.NOT_FOUND),
    // 내부 서버 오류
    INTERNAL_SERVER_ERROR(404, "내부 서버 오류", HttpStatus.NOT_FOUND),
    SQL_ERROR(404, "SQL 오류", HttpStatus.NOT_FOUND),
    DATA_ACCESS_ERROR(404, "sql 접근 에러", HttpStatus.NOT_FOUND)
    ;
    private final int code;
    private final String message;
    private final HttpStatus httpStatus;
}
