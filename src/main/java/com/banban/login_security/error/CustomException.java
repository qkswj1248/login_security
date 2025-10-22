package com.banban.login_security.error;

import com.banban.login_security.code.Code;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
    private final Code code;
    private final Object object;

    public CustomException(Code code){
        this.code = code;
        this.object = null;
    }

    public CustomException(Code code, Object object){
        this.code = code;
        this.object = object;
    }

}
