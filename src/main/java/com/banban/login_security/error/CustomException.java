package com.banban.login_security.error;

import com.banban.login_security.code.Code;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
    private final Code code;
    private final String message;
    private final Object object;

    public CustomException(Code code, String message){
        this.code = code;
        this.message = message;
        this.object = null;
    }

    public CustomException(Code code, String message, Object object){
        this.code = code;
        this.message = message;
        this.object = object;
    }

}
