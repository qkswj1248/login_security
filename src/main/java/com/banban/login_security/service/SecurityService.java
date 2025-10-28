package com.banban.login_security.service;

import com.banban.login_security.domain.LoginMember;
import com.banban.login_security.domain.TokenInfo;

public interface SecurityService {
    public TokenInfo createTokens(LoginMember loginMember);

    public TokenInfo createAccessToken(String refreshToken);

    public String getRefreshToken(String refreshToken);

    public void addRefreshToken(String refreshToken);
}
