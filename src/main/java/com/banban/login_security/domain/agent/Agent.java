package com.banban.login_security.domain.agent;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter @Setter
@NoArgsConstructor
public class Agent {
    private Long id;
    private Long user_id;
    private String name;
    private String status;
    private OffsetDateTime created_at;

    public Agent(Long id, Long user_id, String name, String status, OffsetDateTime created_at){
        this.id = id;
        this.user_id = user_id;
        this.name = name;
        this.status = status;
        this.created_at = created_at;
    }

}
