package com.banban.login_security.controller;

import com.banban.login_security.code.SecurityErrorCode;
import com.banban.login_security.code.SuccessCode;
import com.banban.login_security.domain.LoginWebDetails;
import com.banban.login_security.domain.member.LoginUserInfo;
import com.banban.login_security.domain.member.Member;
import com.banban.login_security.domain.Response;
import com.banban.login_security.domain.TokenInfo;
import com.banban.login_security.error.CustomException;
import com.banban.login_security.service.MemberService;
import com.banban.login_security.service.RefreshTokenService;
import com.banban.login_security.service.SecurityService;
import com.banban.login_security.util.CommonFunction;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/users")
public class UserController {

    private final MemberService memberService;
    private final SecurityService securityService;
    private final RefreshTokenService refreshTokenService;
    private final CommonFunction commonFunction;

    @PostMapping(value = "/join", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> joinForLocal(@RequestBody Member member){
        memberService.addMemberForLocal(member);
        log.info("회원가입 완료 : {}", member.getId());
        return Response.toResponseEntity(SuccessCode.CREATED_SUCCESS, null);
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> loginForJson(@RequestBody LoginUserInfo loginMember, HttpServletRequest request){
        LoginWebDetails details = commonFunction.toLoginWebDetails(request);
        log.info("ip : {}, agent : {}, deviceId : {}", details.ip(), details.agent(), details.deviceId());

        // token 생성
        TokenInfo tokenInfo = securityService.createTokensForLogin(loginMember, details);

        ResponseCookie accessToken = Response.toCookie("accessToken", tokenInfo.accessToken(), 3600);
        ResponseCookie refreshToken = Response.toCookie("refreshToken", tokenInfo.refreshToken(), 60 * 60 * 24 * 7);
        ResponseCookie deviceIdCookie = Response.toCookie("deviceId", tokenInfo.deviceId(), 60 * 60 * 24 * 7);

        // token 포함해서 반환
        return Response.toResponseEntity(SuccessCode.LOGIN_SUCCESS, null, accessToken, refreshToken, deviceIdCookie);
    }

    @PostMapping(value = "/auth")
    public ResponseEntity<Response> authForJson(HttpServletRequest request){
        String token = CommonFunction.getCookie(request, "refreshToken");
        if(token == null){
            throw new CustomException(SecurityErrorCode.NOT_COOKIE, "쿠키가 존재하지 않습니다.");
        }

        LoginWebDetails details = commonFunction.toLoginWebDetails(request);
        log.info("ip : {}, agent : {}, deviceId : {}", details.ip(), details.agent(), details.deviceId());

        TokenInfo tokenInfo = securityService.createTokensForRef(token, details);

        ResponseCookie accessToken = Response.toCookie("accessToken", tokenInfo.accessToken(), 60);
        ResponseCookie refreshTokenCookie = Response.toCookie("refreshToken", tokenInfo.refreshToken(), 60 * 60 * 24);
        ResponseCookie deviceIdCookie = Response.toCookie("deviceId", tokenInfo.deviceId(), 60 * 60 * 24 * 7);

        log.info("토큰 재발급 성공");
        return Response.toResponseEntity(SuccessCode.AUTH_SUCCESS, null, accessToken, refreshTokenCookie);
    }

    @PostMapping(value = "/auth_test")
    public ResponseEntity<Response> authTest(){
        log.info("토큰 인증 성공");
        return Response.toResponseEntity(SuccessCode.AUTH_SUCCESS, null);
    }

}
