package com.banban.login_security.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/*

    OncePerRequestFilter 는 한번만 호출되기 때문에 권장된다

    Jwt 의 핵심 필터
    join, login url 을 제외한 다른 접근은 다 이 필터를 거치게 해놨다
    인증이 되지 않으면 jwt 예외 발생
 */
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try{
            // Request Header 에서 토큰 추출하기(없으면 null 반환)

            // null 인지 확인하고 validateToken 으로 토큰 유효성 검사

            // 인증이 된다면 ContextHolder 에 담아두기
            // 여기서 안담겨진(인증이 안된) 것들은 다음 필터에서 확인하고 error 를 던져줌
        }catch (Exception e){
            logger.warn("JWT 처리 중 예외 발생 : {}", e);
        }
        filterChain.doFilter(request, response);
    }
}
