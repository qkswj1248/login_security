package com.banban.login_security.domain.member;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class LoginMemberForProvider {
    private final String provider;
    private final String accessToken;

    public LoginMemberForProvider(String provider, String accessToken){
        this.provider = provider;
        this.accessToken = accessToken;
    }
}
