package com.banban.login_security.domain.agent;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Builder
public class FindAgent {
    private Long user_id;
    private String name;

    public static FindAgent create(Long user_id, String name){
        return FindAgent.builder()
                .user_id(user_id)
                .name(name)
                .build();
    }
}
