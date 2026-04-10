package com.banban.login_security.service;

import com.banban.login_security.domain.member.FindMember;
import com.banban.login_security.domain.member.Member;
import java.util.Optional;

public interface MemberService {

    public Optional<Member> findMemberForLocal(FindMember findMember);
    public Optional<Member> findMemberForSocial(FindMember findMember);
    public void addMemberForLocal(Member member);
}
