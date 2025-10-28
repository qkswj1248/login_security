package com.banban.login_security.service;

import com.banban.login_security.code.CommonErrorCode;
import com.banban.login_security.code.SecurityErrorCode;
import com.banban.login_security.domain.LoginMember;
import com.banban.login_security.domain.TokenInfo;
import com.banban.login_security.error.CustomException;
import com.banban.login_security.mapper.MemberMapper;
import com.banban.login_security.mapper.RefreshTokenMapper;
import com.banban.login_security.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class SecurityServiceImpl implements SecurityService{

    private final MemberMapper memberMapper;
    private final RefreshTokenMapper refreshTokenMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    @Override
    public TokenInfo createTokens(LoginMember loginMember) {
        /*
        id랑 pw로 Authentication 객체 생성
        JwtTokenProvider의 User~Token은 인증"완료"된 객체이고
        여기 User~Token은 인증"전"객체

       authenticationManager 는 인증을 처리핳는 매니저로
       여기서 인증을 처리(UserDetailsService 도 여기서 사용)
         */
        UsernamePasswordAuthenticationToken authenticationToken
                = new UsernamePasswordAuthenticationToken(loginMember.getId(), loginMember.getPassword());
        try{
            Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
            return jwtTokenProvider.createTokens(authentication);
        }catch (AuthenticationException e){
            throw new CustomException(SecurityErrorCode.SECURITY_AUTH_WRONG);
        }
    }

    @Override
    public TokenInfo createAccessToken(String refreshToken) {
        // refresh token 저장소 확인
        if(refreshToken == null){
            throw new CustomException(SecurityErrorCode.NO_HEADER_REFRESH_TOKEN);
        }
        getRefreshToken(refreshToken); // 없으면 Exception 발생

        // access token 발급을 위해 refresh token 에서 유저 아이디 얻기


        // access token 발급

        return null;
    }

    @Override
    public String getRefreshToken(String refreshToken) {
        return refreshTokenMapper.findRefreshToken(refreshToken)
                .filter(v -> !v.isBlank()) // null, "", "   " 모두제외 (더욱 강화)
                .orElseThrow(() -> new CustomException(SecurityErrorCode.REFRESH_TOKEN_NOT_EXIST));
    }

    @Override
    public void addRefreshToken(String refreshToken) {
        try{
            refreshTokenMapper.insert(refreshToken);
        }catch (DataAccessException e){
            throw new CustomException(CommonErrorCode.DATA_ACCESS_ERROR, e);
        }
    }
}
