package com.banban.login_security.domain;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Member {
    private String id;
    private String password;
    private String name;

    public Member(String id, String name, String password){
        this.id = id;
        this.name = name;
        this.password = password;
    }
}
