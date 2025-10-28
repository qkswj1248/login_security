package com.banban.login_security.security;

import com.banban.login_security.code.Code;
import com.banban.login_security.code.SecurityErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.util.Map;

public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        /*
        BadCredentailException은 AbstractUserDetailsAuthenticationProvider의
        .autentication 이라는 함수(userDetails를 사용하는 인증의 핵심 함수)에서
        UserDetails가 UserNotFoundException을 던지면 throw하는 Exception이다!
        */
        if(authException instanceof BadCredentialsException){
            setResponse(response, SecurityErrorCode.SECURITY_PASSWORD_IS_WRONG);
        }else if(authException instanceof InsufficientAuthenticationException){
            setResponse(response, SecurityErrorCode.CUSTOM_EXPIRED_TOKEN);
        }else{
            setResponse(response, SecurityErrorCode.UNKNOWN_ERROR);
        }
    }

    /*
    이 클래스는 controller 이전에 실행되는 클래스이기 때문에
    응답을 따로 작성해야함...
     */
    private void setResponse(HttpServletResponse response, Code code) throws IOException{
        Map<String, Object> data = Map.of(
                "name", code.name(),
                "code", code.getCode(),
                "message", code.getMessage()
        );
        final ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), data);
    }

}
