package com.banban.login_security.domain;

import com.banban.login_security.code.Code;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.ResponseEntity;

/*
응답할 JSON 형태
{
    "code" : "OK",
    "message" : "로그인 성공",
    "data" : {
        "~" : "~",
        "~" : "~",
    }
}
 */
@Getter
@Builder
public class Response{
    private final int code;
    private final String message;
    // null 값이나 length 가 0인 값들을 제외시키도록 조정할 수 있는 어노테이션
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Object detail;

    public static ResponseEntity<Response> toResponseEntity(Code code, Object object){
        return ResponseEntity
                .status(code.getCode())
                .body(Response.builder()
                        .code(code.getCode())
                        .message(code.getMessage())
                        .detail(object)
                        .build()
                );
    }
}
