package com.banban.login_security.service;

import com.banban.login_security.domain.Member;
import java.util.Optional;

public interface MemberService {
    public Optional<Member> findMember(String id);
    public void addMember(Member member);
}
