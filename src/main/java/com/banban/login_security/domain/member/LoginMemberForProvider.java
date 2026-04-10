package com.banban.login_security.domain.member;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class LoginMemberForProvider {
    String provider;
    String accessToken;

    public LoginMemberForProvider(String provider, String accessToken){
        this.provider = provider;
        this.accessToken = accessToken;
    }
}
