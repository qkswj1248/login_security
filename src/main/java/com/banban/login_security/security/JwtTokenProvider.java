package com.banban.login_security.security;


import com.banban.login_security.domain.TokenInfo;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
public class JwtTokenProvider {
    private final Key key;
    private static final int SECOND = 60000 * 5; // 60000 = 1분

    /*
        yml 키를 받아서 풀고 난 다음 저장하는 생성자
        @Value : properties 나 yml 에 있는 값을 가져오는 방법
        Value 에러 이유 -> 라이브러리 lombok.Value 로 되어있었음^^
    */
    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey){
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    // request 에서 Auth 를 가져와 반환 (없으면 null 반환)
    public String resolveAccessToken(HttpServletRequest request){
        return request.getHeader("Authorization");
    }

    // refreshToken 반환
    public String resolveRefreshToken(HttpServletRequest request){
        return request.getHeader("RefreshToken");
    }

    // 로그인 인증에 성공하면 access 랑 refresh 토큰 만들어주는 메소드
    public TokenInfo createTokens(Authentication authentication){

        /*
        getAuthorities() 로 GrantedAuthority 를 포함한 collection 을 반환함
        collection 에서 GrantedAuthority 안에 있는 getAuthority 만 가져오기!
        가져온 값들 사이에 "," 넣고 String 으로 변환!
         */
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        Date accessTokenExpiresIn = new Date(System.currentTimeMillis() + SECOND);
        Date refreshTokenExpiresIn = new Date(System.currentTimeMillis() + SECOND * 10);

        String accessToken = createJwtToken(authentication, authorities, accessTokenExpiresIn);
        String refreshToken = createJwtToken(authentication, authorities, refreshTokenExpiresIn);

        return TokenInfo.of(accessToken, refreshToken, "Bearer");
    }

    // refresh token 이랑 access token 분리해서 따로 만들기

    // 이유 : refresh token 만 받았을 때는 access token 만 새로 밠급해야하니까
    public String createRefreshToken(Authentication authentication){
        return "";
    }

    public String createJwtToken(Authentication authentication, String auths, Date date){
        return Jwts.builder()
                .setSubject(authentication.getName())
                .claim("auth", auths)
                .setExpiration(date)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    //--------------------------------------------
    // jwt 복호화해서 토큰 정보 꺼내고 인증받는 핵심 메서드
    // -------------------------------------------
    public Authentication getAuthentication(String accessToken){
        // 먼저 토큰 복호화
        Claims claims = parseClaims(accessToken);
        if(claims.get("auth") == null){
            log.warn("권한 정보가 없는 토큰입니다.");
        }

        // claim 에서 권한 정보 가져와서 리스트로 만들기
        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get("auth").toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .toList();

        // UserDetails 개체를 만들어서 Authentication(인증) 리턴
        UserDetails principal = new User(claims.getSubject(), "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    public boolean validateToken(String token){
        try{
            Claims claims = parseClaims(token);
            return true;
        }catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e){
            log.warn("잘못된 JWT 서명입니다.");
        }catch (ExpiredJwtException e){
            log.warn("만료된 JWT 토큰입니다.");
        }catch (UnsupportedJwtException e){
            log.warn("지원되지 않는 JWT 토큰입니다.");
        }catch (IllegalArgumentException e){
            log.warn("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }

    public Claims parseClaims(String token){
        return Jwts.parserBuilder()
                .setSigningKey(key).build()
                .parseClaimsJws(token).getBody();
    }


}
