package com.banban.login_security.service;

import com.banban.login_security.domain.Reissue;
import com.banban.login_security.domain.member.LoginUserInfo;
import com.banban.login_security.domain.RefreshToken;
import com.banban.login_security.domain.TokenInfo;

public interface SecurityService {
    public TokenInfo createTokensForLogin(LoginUserInfo loginMember);

    public TokenInfo createTokensForRef(Reissue refreshToken);

    public RefreshToken getRefreshToken(Long userId);

    public void addRefreshToken(RefreshToken refreshToken);
}
