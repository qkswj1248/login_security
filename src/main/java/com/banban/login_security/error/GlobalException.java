package com.banban.login_security.error;

import com.banban.login_security.domain.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class GlobalException extends ResponseEntityExceptionHandler {
    // 내가 설정한 예외를 잡아서 처리(controller 단의)
    @ExceptionHandler(CustomException.class)
    private ResponseEntity<Response> handleCustomException(CustomException e){
        log.warn("customException : {}", e.getCode().getMessage());
        return Response.toResponseEntity(e.getCode(), e.getObject());
    }

    // 언체크 예외를 잡아서 처리
    // illegal 은 적절하지 못한 인자를 메소드에 넘겨주었을 때 발생한다.
    // @Valid에 의한 유효성 검증에 실패했을 때 발생함(??)

}
