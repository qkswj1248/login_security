package com.banban.login_security.code;

import org.springframework.http.HttpStatus;

public interface Code {
    int getCode();
    String getMessage();
    HttpStatus getHttpStatus();
}
