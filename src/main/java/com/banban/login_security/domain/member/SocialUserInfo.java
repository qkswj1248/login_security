package com.banban.login_security.domain.member;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SocialUserInfo {
    private String provider;
    private String providerId;
    private String email;

    public SocialUserInfo(String provider, String providerId, String email){
        this.provider = provider;
        this.providerId = providerId;
        this.email = email;
    }

}
