package com.banban.login_security.mapper;

import com.banban.login_security.domain.member.FindMember;
import com.banban.login_security.domain.member.Member;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;
/*
    Optional은 값이 없을 수 있을 때(null) 사용하는 것으로
    null을 직접 다루지 않고 orElse로 예외를 던져버릴 수 있음
 */
@Mapper
public interface MemberMapper {
    Optional<Member> findMemberByEmail(FindMember member);
    Optional<Member> findMemberByProvider(FindMember member);
    void insert(Member member);
}
