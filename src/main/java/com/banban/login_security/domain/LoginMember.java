package com.banban.login_security.domain;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class LoginMember {
    String id;
    String password;

    public LoginMember(String id, String password){
        this.id = id;
        this.password = password;
    }
}
