package com.banban.login_security.security;

import com.banban.login_security.code.SecurityErrorCode;
import lombok.Getter;
import org.springframework.security.core.AuthenticationException;


@Getter
public class JwtAuthenticationException extends AuthenticationException {
    private final SecurityErrorCode errorCode;
    private final Object object;

    public JwtAuthenticationException(SecurityErrorCode errorCode, Object object){
        super(errorCode.name());
        this.errorCode = errorCode;
        this.object = object;
    }

    public JwtAuthenticationException(SecurityErrorCode errorCode){
        super(errorCode.name());
        this.errorCode = errorCode;
        this.object = null;
    }

}
