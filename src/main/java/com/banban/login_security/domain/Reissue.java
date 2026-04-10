package com.banban.login_security.domain;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Reissue {
    String refreshToken;

    public Reissue(String refreshToken){
        this.refreshToken = refreshToken;
    }
}
