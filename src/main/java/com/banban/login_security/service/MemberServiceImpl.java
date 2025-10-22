package com.banban.login_security.service;

import com.banban.login_security.code.CommonErrorCode;
import com.banban.login_security.code.UserErrorCode;
import com.banban.login_security.domain.Member;
import com.banban.login_security.error.CustomException;
import com.banban.login_security.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService{

    private final MemberMapper memberMapper;

    @Override
    public Optional<Member> findMember(String id) {
        return memberMapper.findById(id);
    }

    @Override
    public void addMember(Member member) {
        // 이미 가입된 사용자인지 확인하기
        if(findMember(member.getId()).isPresent()){
            throw new CustomException(UserErrorCode.EXISTING_USER);
        }
        // 없는 사용자라면 가입처리
        try{
            memberMapper.insert(member);
        }catch (DataAccessException e){
            throw new CustomException(CommonErrorCode.DATA_ACCESS_ERROR, e.getMessage());
        }
    }
}
