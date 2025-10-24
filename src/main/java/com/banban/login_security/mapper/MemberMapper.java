package com.banban.login_security.mapper;

import com.banban.login_security.domain.Member;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;
/*
    Optional은 값이 없을 수 있을 때(null) 사용하는 것으로
    null을 직접 다루지 않고 orElse로 예외를 던져버릴 수 있음
 */
@Mapper
public interface MemberMapper {
    List<Member> findAll();
    Optional<Member> findById(@Param("id") String id);
    void insert(Member member);
}
