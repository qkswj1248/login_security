package com.banban.login_security.domain.member;

import com.banban.login_security.type.ProviderType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class Member {
    private Long id;
    private String email;
    private String password;
    private String name;
    private String provider;
    private String providerId;
    private String auth = "basic";

    public Member(Long id, String email, String password, String name, String provider, String providerId, String auth){
        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
        this.provider = provider;
        this.providerId = providerId;
        this.auth = auth;
    }
}
