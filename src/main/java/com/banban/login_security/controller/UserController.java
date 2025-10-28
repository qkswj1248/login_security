package com.banban.login_security.controller;

import com.banban.login_security.code.SuccessCode;
import com.banban.login_security.domain.LoginMember;
import com.banban.login_security.domain.Member;
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
    public ResponseEntity<Response> joinForJson(@RequestBody Member member){
        memberService.addMember(member);
        log.info("회원가입 완료 : {}", member.getId());
        return Response.toResponseEntity(SuccessCode.CREATED_SUCCESS, null);
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> loginForJson(@RequestBody LoginMember loginMember){
        // token 생성
        TokenInfo tokenInfo = securityService.createTokens(loginMember);
        // token 포함해서 반환
        return Response.toResponseEntity(SuccessCode.LOGIN_SUCCESS, tokenInfo);
    }



}
