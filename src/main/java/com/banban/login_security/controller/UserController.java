package com.banban.login_security.controller;

import com.banban.login_security.code.SuccessCode;
import com.banban.login_security.domain.Reissue;
import com.banban.login_security.domain.member.LoginUserInfo;
import com.banban.login_security.domain.member.Member;
import com.banban.login_security.domain.Response;
import com.banban.login_security.domain.TokenInfo;
import com.banban.login_security.service.MemberService;
import com.banban.login_security.service.SecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/users")
public class UserController {

    private final MemberService memberService;
    private final SecurityService securityService;

    @PostMapping(value = "/join", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> joinForLocal(@RequestBody Member member){
        memberService.addMemberForLocal(member);
        log.info("회원가입 완료 : {}", member.getId());
        return Response.toResponseEntity(SuccessCode.CREATED_SUCCESS, null);
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> loginForJson(@RequestBody LoginUserInfo loginMember){
        // token 생성
        TokenInfo tokenInfo = securityService.createTokensForLogin(loginMember);
        // token 포함해서 반환
        return Response.toResponseEntity(SuccessCode.LOGIN_SUCCESS, tokenInfo);
    }

    @PostMapping(value = "/auth", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> authForJson(@RequestBody(required = true) Reissue refreshToken){
        TokenInfo tokenInfo = securityService.createTokensForRef(refreshToken);
        // refresh token 확인하기

        return Response.toResponseEntity(SuccessCode.AUTH_SUCCESS, tokenInfo);
    }

    @PostMapping(value = "/auth_test")
    public ResponseEntity<Response> authTest(){
        log.info("auth_test 성공!");
        return Response.toResponseEntity(SuccessCode.AUTH_SUCCESS, null);
    }

}
