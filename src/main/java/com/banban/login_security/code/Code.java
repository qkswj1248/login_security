package com.banban.login_security.code;

import org.springframework.http.HttpStatus;

public interface Code {
    String name();
    String getCode();
    String getMessage();
    HttpStatus getHttpStatus();
}
