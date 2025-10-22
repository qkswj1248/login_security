package com.banban.login_security.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements Code{
    USER_NOT_FOUND(404, "해당 유저 정보가 없습니다.", HttpStatus.NOT_FOUND),
    USER_PASSWORD_FAIL(404, "비밀번호가 맞지 않습니다.", HttpStatus.NOT_FOUND),
    EXISTING_USER(404, "이미 존재하는 유저입니다.", HttpStatus.NOT_FOUND),
    ;
    private final int code;
    private final String message;
    private final HttpStatus httpStatus;
}
