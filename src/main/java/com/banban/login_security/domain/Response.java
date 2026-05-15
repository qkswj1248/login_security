package com.banban.login_security.domain;

import com.banban.login_security.code.Code;
import com.banban.login_security.code.SuccessCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
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
    private final String name;
    private final String code;
    private final String message;
    // null 값이나 length 가 0인 값들을 제외시키도록 조정할 수 있는 어노테이션
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Object detail;

    /*
     프론트에게는 에러 내용이 자세히 나오면 안되니까.. 음
     */
    public static ResponseEntity<Response> toResponseEntity(Code code, Object object){
        String message = code.getMessage();
//        if(!(code instanceof SuccessCode)) {
//            message = "요청 처리 중 문제가 발생하였습니다.";
//        }
        return ResponseEntity
                .status(code.getHttpStatus())
                .body(Response.builder()
                        .name(code.name())
                        .code(code.getCode())
                        .message(message)
                        .detail(object)
                        .build()
                );
    }

    public static ResponseEntity<Response> toResponseEntity(Code code, Object object, ResponseCookie... cookies){
        String message = code.getMessage();
        HttpHeaders headers = new HttpHeaders();
        for(ResponseCookie cookie : cookies){
            headers.add(HttpHeaders.SET_COOKIE, cookie.toString());
        }
        return ResponseEntity
                .status(code.getHttpStatus())
                .headers(headers)
                .body(Response.builder()
                        .name(code.name())
                        .code(code.getCode())
                        .message(message)
                        .detail(object)
                        .build()
                );
    }

    public static ResponseCookie toCookie(String name, String cookie, int maxAge){
        return ResponseCookie
                .from(name, cookie)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(maxAge)
                .build();
    }
}
