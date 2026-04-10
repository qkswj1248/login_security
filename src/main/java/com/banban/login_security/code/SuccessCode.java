package com.banban.login_security.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SuccessCode implements Code{
    OK("", "요청이 성공적으로 처리되었습니다"),
    CREATED_SUCCESS("", "회원가입이 완료되었습니다"),
    AUTH_SUCCESS("", "토큰 인증이 정상적으로 완료되었습니다"),
    LOGIN_SUCCESS("", "로그인이 정상적으로 처리되었습니다");
    // enum은 불변성이 중요하므로 private final 붙이기
    private final String code;
    private final String message;
    private final HttpStatus httpStatus = HttpStatus.OK;
}
