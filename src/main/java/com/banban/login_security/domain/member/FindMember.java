package com.banban.login_security.domain.member;

import com.banban.login_security.type.ProviderType;
import com.banban.login_security.type.Type;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Builder
public class FindMember {
    String provider;
    String email;
    String providerId;

    public static FindMember createLocalFindMember(String email){
        return FindMember.builder()
                .provider(ProviderType.LOCAL.getType())
                .email(email)
                .build();
    }

    public static FindMember createSocialFindMember(Type provider, String providerId){
        return FindMember.builder()
                .provider(provider.getType())
                .providerId(providerId)
                .build();
    }
}
