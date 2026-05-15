package com.banban.login_security.security;

import com.banban.login_security.code.SecurityErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/*

    OncePerRequestFilter 는 한번만 호출되기 때문에 권장된다

    Jwt 의 핵심 필터
    join, login url 을 제외한 다른 접근은 다 이 필터를 거치게 해놨다
    인증이 되지 않으면 jwt 예외 발생
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public final JwtTokenProvider jwtTokenProvider;
    public final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private static final String ACCESS_TOKEN = "Authorization";
    private static final String REFRESH_TOKEN = "RefreshToken";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try{
            // Request Header 에서 토큰 추출하기(없으면 null 반환)
            String accessToken = jwtTokenProvider.resolveAccessToken(request);
            String path = request.getRequestURI();

            if(path.equals("/users/login") || path.equals("/users/join") || path.equals("/users/auth")){
                filterChain.doFilter(request, response);
                return;
            }

            // null 인지 확인하고 validateToken 으로 토큰 유효성 검사
            if(StringUtils.hasText(accessToken) && jwtTokenProvider.validateToken(accessToken)){
                Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);
                // 인증이 된다면 ContextHolder 에 담아두기
                // 이렇게 만든 인증토큰 담아두면 다음 필터들이 인증된걸 알고 넘어가줌
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }catch (JwtAuthenticationException e) {
            log.warn("JWT 인증 중 예외 발생 : {}", e.getMessage());
            jwtAuthenticationEntryPoint.commence(request, response, e);
            return;
        }
        filterChain.doFilter(request, response);
    }
}
