package com.banban.login_security.domain.member;

import com.banban.login_security.type.ProviderType;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Member {
    private Long id;
    private String email;
    private String password;
    private String name;
    private String provider;
    private String providerId;
    private String auth;

    public Member(Long id, String email, String password, String name, String provider, String providerId, String auth){
        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
        this.provider = provider;
        this.providerId = providerId;
        this.auth = auth;
    }

    public Member(String email, String password, String name, String provider, String providerId){
        this.email = email;
        this.password = password;
        this.name = name;
        this.provider = provider;
        if(!provider.equals(ProviderType.LOCAL.getType())){
            this.providerId = providerId;
        }
        this.auth = "basic";
    }
}
