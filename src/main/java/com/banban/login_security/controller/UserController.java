package com.banban.login_security.controller;

import com.banban.login_security.code.SuccessCode;
import com.banban.login_security.domain.Member;
import com.banban.login_security.domain.Response;
import com.banban.login_security.service.MemberService;
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

    @PostMapping(value = "/join", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> joinForJson(@RequestBody Member member){
        memberService.addMember(member);
        log.info("회원가입 완료 : {}", member.getId());
        return Response.toResponseEntity(SuccessCode.CREATED_SUCCESS, null);
    }


}
