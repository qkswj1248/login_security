package com.banban.login_security.domain.member;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class LoginUserInfo {
    String email;
    String password;

    public LoginUserInfo(String email, String password){
        this.email = email;
        this.password = password;
    }
}
