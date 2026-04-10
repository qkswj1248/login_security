package com.banban.login_security.service;

import com.banban.login_security.code.SecurityErrorCode;
import com.banban.login_security.domain.member.FindMember;
import com.banban.login_security.domain.member.Member;
import com.banban.login_security.error.CustomException;
import com.banban.login_security.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetailsService {

    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        FindMember findMember = FindMember.createLocalFindMember(email);

        Member member = memberMapper.findMemberByEmail(findMember)
                .orElseThrow(() -> new CustomException(SecurityErrorCode.SECURITY_USER_NOT_FOUND));

        return createUserDetails(member);
    }

    private UserDetails createUserDetails(Member member){
        return User.builder()
                .username(member.getId().toString())
                .password(member.getPassword())
                .roles(member.getAuth())
                .build();
    }

}

