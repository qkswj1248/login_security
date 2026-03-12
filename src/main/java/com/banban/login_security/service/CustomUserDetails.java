package com.banban.login_security.service;

import com.banban.login_security.code.SecurityErrorCode;
import com.banban.login_security.domain.Member;
import com.banban.login_security.error.CustomException;
import com.banban.login_security.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetailsService {

    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return memberMapper.findById(username)
                .map(this::createUserDetails)
                .orElseThrow(() -> new CustomException(SecurityErrorCode.SECURITY_USER_NOT_FOUND));
    }

    private UserDetails createUserDetails(Member member){
        return User.builder()
                .username(member.getId())
                .password(member.getPassword())
                .roles(member.getAuth())
                .build();
    }

}

