package com.banban.login_security.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SuccessCode implements Code{
    OK(200, "요청이 성공적으로 처리되었습니다", HttpStatus.OK),
    CREATED_SUCCESS(201, "회원가입이 완료되었습니다", HttpStatus.OK),
    LOGIN_SUCCESS(202, "로그인이 정상적으로 처리되었습니다", HttpStatus.OK);
    // enum은 불변성이 중요하므로 private final 붙이기
    private final int code;
    private final String message;
    private final HttpStatus httpStatus;
}
