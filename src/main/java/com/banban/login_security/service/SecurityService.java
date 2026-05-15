package com.banban.login_security.service;

import com.banban.login_security.domain.LoginWebDetails;
import com.banban.login_security.domain.member.LoginUserInfo;
import com.banban.login_security.domain.RefreshToken;
import com.banban.login_security.domain.TokenInfo;

public interface SecurityService {
    public TokenInfo createTokensForLogin(LoginUserInfo loginMember, LoginWebDetails details);

    public TokenInfo createTokensForRef(String refreshToken, LoginWebDetails details);
}
