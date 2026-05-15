package com.banban.login_security.service;

import com.banban.login_security.code.SecurityErrorCode;
import com.banban.login_security.domain.*;
import com.banban.login_security.domain.member.LoginUserInfo;
import com.banban.login_security.error.CustomException;
import com.banban.login_security.mapper.RefreshTokenMapper;
import com.banban.login_security.security.JwtTokenProvider;
import com.banban.login_security.security.risk.LoginAnalysisDataProvider;
import com.banban.login_security.security.risk.RiskAnalyzer;
import com.banban.login_security.type.Auth;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityServiceImpl implements SecurityService{

    private final RefreshTokenMapper refreshTokenMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final RefreshTokenService refreshTokenService;
    private final LoginHistoryService loginHistoryService;
    private final ApplicationEventPublisher publisher;
    private final RiskAnalyzer riskAnalyzer;
    private final LoginAnalysisDataProvider loginAnalysisDataProvider;

    public Authentication getAuthentication(UsernamePasswordAuthenticationToken authenticationToken){
                /*
        id랑 pw로 Authentication 객체 생성
        JwtTokenProvider의 User~Token은 인증"완료"된 객체이고
        여기 User~Token은 인증"전"객체

       authenticationManager 는 인증을 처리하는 매니저로
       여기서 인증을 처리(UserDetailsService 도 여기서 사용)
         */
        try{
            // 아이디랑 비번 확인중
            return authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        }catch (AuthenticationException e){
            throw new CustomException(SecurityErrorCode.SECURITY_AUTH_WRONG, "아이디 비번 확인 중 문제 발생");
        }
    }

    public RefreshToken getRefreshToken(Long userId, LoginWebDetails details){
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userId, details);
        if(!refreshTokenService.isExistingDeviceId(userId, details.deviceId())){
            refreshToken.setDeviceId(refreshTokenService.createDeviceId());
            refreshTokenService.loginWithNewDevice(userId, refreshToken);
        }else{
            refreshTokenService.loginWithExistingDevice(userId, refreshToken);
        }
        return refreshToken;
    }

    @Override
    public TokenInfo createTokensForLogin(LoginUserInfo loginMember, LoginWebDetails details) {
        UsernamePasswordAuthenticationToken authenticationToken
                = new UsernamePasswordAuthenticationToken(loginMember.email(), loginMember.password());
        authenticationToken.setDetails(details);

        Authentication authentication = getAuthentication(authenticationToken);

        Long userId = Long.parseLong(authentication.getName());

        String accessToken = jwtTokenProvider.createAccessToken(userId.toString(), Auth.BASIC);
        RefreshToken refreshToken = getRefreshToken(userId, details);

        LoginHistory loginHistory = new LoginHistory(userId, authentication.getName(), details.ip(), details.agent(), refreshToken.getDeviceId(), OffsetDateTime.now(), true, "");

        publisher.publishEvent(loginHistory);
        return new TokenInfo(accessToken, refreshToken.getToken(), refreshToken.getDeviceId());
    }

    @Override
    public TokenInfo createTokensForRef(String preRef, LoginWebDetails details) {
        // 토큰 인증
        Claims claims = validateToken(preRef);
        // ID 얻기
        Long userId = Long.parseLong(claims.getSubject());
        // refresh token db 확인
        RefreshToken findRef = refreshTokenService.getRefreshTokenForDeviceId(userId, details.deviceId())
                .orElseThrow(() -> new CustomException(SecurityErrorCode.REFRESH_TOKEN_NOT_EXIST, "refreshToken 존재 안함"));// 없으면 Exception 발생
        // 다르다면 모든 ref 지우고 exception 발생!
        if(!findRef.getToken().equals(preRef)){
            refreshTokenMapper.deleteForUserId(userId);
            publisher.publishEvent(new LoginHistory(userId, "", details.ip(), details.agent(), details.deviceId(), OffsetDateTime.now(), false, "ref로 재발급 중 실패"));
            throw new CustomException(SecurityErrorCode.STOLEN_REFRESH_TOKEN, "RefreshToken 다름");
        }
        // rtr : refresh token 새로 발급
        String accessToken = jwtTokenProvider.createAccessToken(userId.toString(), Auth.BASIC);
        RefreshToken refreshToken = getRefreshToken(userId, details);



        publisher.publishEvent(new LoginHistory(userId, "", details.ip(), details.agent(), refreshToken.getDeviceId(), OffsetDateTime.now(), true, ""));
        return new TokenInfo(accessToken, refreshToken.getToken(), refreshToken.getDeviceId());
    }

    public Claims validateToken(String token) {
        try{
            return jwtTokenProvider.parseClaims(token);
        } catch (ExpiredJwtException e) {
            throw new CustomException(SecurityErrorCode.CUSTOM_EXPIRED_TOKEN, "만료된 토큰입니다.");
        } catch (UnsupportedJwtException e){
            throw new CustomException(SecurityErrorCode.CUSTOM_UNSUPPORTED_TOKEN, "지원되지 않는 토큰입니다.");
        } catch (JwtException | IllegalArgumentException e){
            throw new CustomException(SecurityErrorCode.CUSTOM_WRONG_TYPE_TOKEN, "잘못된 토큰 타입입니다.");
        }
    }
}
