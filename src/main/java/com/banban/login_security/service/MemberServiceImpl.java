package com.banban.login_security.service;

import com.banban.login_security.code.CommonErrorCode;
import com.banban.login_security.code.UserErrorCode;
import com.banban.login_security.domain.member.FindMember;
import com.banban.login_security.domain.member.Member;
import com.banban.login_security.error.CustomException;
import com.banban.login_security.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService{

    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Optional<Member> findMemberForLocal(FindMember findMember) {
        return memberMapper.findMemberByEmail(findMember);
    }

    @Override
    public Optional<Member> findMemberForSocial(FindMember findMember) {
        return memberMapper.findMemberByProvider(findMember);
    }

    @Override
    public void addMemberForLocal(Member member) {
        FindMember findMember = FindMember.createLocalFindMember(member.getEmail());
        // 이미 가입된 사용자인지 확인하기
        if(findMemberForLocal(findMember).isPresent()){
            throw new CustomException(UserErrorCode.EXISTING_USER);
        }
        // 없는 사용자라면 가입처리
        try{
            member.setPassword(passwordEncoder.encode(member.getPassword()));
            memberMapper.insert(member);
        }catch (DataAccessException e){
            throw new CustomException(CommonErrorCode.DATA_ACCESS_ERROR, e.getMessage());
        }
    }
}
