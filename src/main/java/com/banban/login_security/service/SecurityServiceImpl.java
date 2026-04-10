package com.banban.login_security.service;

import com.banban.login_security.code.CommonErrorCode;
import com.banban.login_security.code.SecurityErrorCode;
import com.banban.login_security.domain.Reissue;
import com.banban.login_security.domain.member.LoginUserInfo;
import com.banban.login_security.domain.RefreshToken;
import com.banban.login_security.domain.TokenInfo;
import com.banban.login_security.error.CustomException;
import com.banban.login_security.mapper.RefreshTokenMapper;
import com.banban.login_security.security.JwtTokenProvider;
import com.banban.login_security.type.Auth;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityServiceImpl implements SecurityService{

    private final RefreshTokenMapper refreshTokenMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    public Authentication getAuthentication(LoginUserInfo loginMember){
                /*
        id랑 pw로 Authentication 객체 생성
        JwtTokenProvider의 User~Token은 인증"완료"된 객체이고
        여기 User~Token은 인증"전"객체

       authenticationManager 는 인증을 처리핳는 매니저로
       여기서 인증을 처리(UserDetailsService 도 여기서 사용)
         */
        UsernamePasswordAuthenticationToken authenticationToken
                = new UsernamePasswordAuthenticationToken(loginMember.getEmail(), loginMember.getPassword());
        try{
            return authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        }catch (AuthenticationException e){
            throw new CustomException(SecurityErrorCode.SECURITY_AUTH_WRONG);
        }
    }

    public TokenInfo reissueRefreshToken(Long userId, String accessToken){
        // ref 있으면 지우기
        refreshTokenMapper.deleteForUserId(userId);

        // refresh token 발급받기
        TokenInfo tokenInfo = jwtTokenProvider.createTokenInfo(userId.toString(), accessToken);

        // refresh 등록하기
        RefreshToken newRef = new RefreshToken(userId, tokenInfo.getRefreshToken(), tokenInfo.getExpired(), tokenInfo.getCreated());
        addRefreshToken(newRef);

        return tokenInfo;
    }

    @Override
    public TokenInfo createTokensForLogin(LoginUserInfo loginMember) {
        Authentication authentication = getAuthentication(loginMember);

        String accessToken = jwtTokenProvider.createAccessToken(authentication.getName(), Auth.BASIC);
        return reissueRefreshToken(Long.parseLong(authentication.getName()), accessToken);
    }

    public Claims validateToken(String token) {
        try{
            return jwtTokenProvider.parseClaims(token);
        } catch (ExpiredJwtException e) {
            throw new CustomException(SecurityErrorCode.CUSTOM_EXPIRED_TOKEN);
        } catch (UnsupportedJwtException e){
            throw new CustomException(SecurityErrorCode.CUSTOM_UNSUPPORTED_TOKEN);
        } catch (JwtException | IllegalArgumentException e){
            throw new CustomException(SecurityErrorCode.CUSTOM_WRONG_TYPE_TOKEN);
        }
    }

    @Override
    public TokenInfo createTokensForRef(Reissue reissue) {
        // 토큰 인증
        Claims claims = validateToken(reissue.getRefreshToken());
        // ID 얻기
        Long userId = Long.parseLong(claims.getSubject());
        // refresh token db 확인
        RefreshToken refreshToken = getRefreshToken(userId); // 없으면 Exception 발생
        // 다르다면 모든 ref 지우고 exception 발생!
        if(!refreshToken.getToken().equals(reissue.getRefreshToken())){
            refreshTokenMapper.deleteForUserId(userId);
            throw new CustomException(SecurityErrorCode.STOLEN_REFRESH_TOKEN);
        }
        // rtr : refresh token 새로 발급
        String accessToken = jwtTokenProvider.createAccessToken(userId.toString(), Auth.BASIC);
        return reissueRefreshToken(userId, accessToken);
    }

    @Override
    public RefreshToken getRefreshToken(Long userId) {
        return refreshTokenMapper.findRefreshToken(userId)
//                .filter(v -> !v.isBlank()) // null, "", "   " 모두제외 (더욱 강화)
                .orElseThrow(() -> new CustomException(SecurityErrorCode.REFRESH_TOKEN_NOT_EXIST));
    }

    @Override
    public void addRefreshToken(RefreshToken refreshToken) {
        try{
            refreshTokenMapper.insert(refreshToken);
        }catch (DataAccessException e){
            throw new CustomException(CommonErrorCode.DATA_ACCESS_ERROR, e);
        }
    }
}
