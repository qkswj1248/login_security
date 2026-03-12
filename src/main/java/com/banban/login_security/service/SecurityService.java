package com.banban.login_security.service;

import com.banban.login_security.domain.LoginMember;
import com.banban.login_security.domain.RefreshToken;
import com.banban.login_security.domain.TokenInfo;

public interface SecurityService {
    public TokenInfo createTokens(LoginMember loginMember);

    public TokenInfo createAccessTokenForRef(String refreshToken);

    public RefreshToken getRefreshToken(String refreshToken);

    public void addRefreshToken(RefreshToken refreshToken);
}
