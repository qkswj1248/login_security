package com.banban.login_security.domain.agent;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter @Setter
public class AgentPermission {
    private Long agent_id;
    private String resource;
    private String action;
    private String scope;
    private OffsetDateTime created_at;

    public AgentPermission(Long agent_id, String resource, String action, String scope, OffsetDateTime created_at){
        this.agent_id = agent_id;
        this.resource = resource;
        this.action = action;
        this.scope = scope;
        this.created_at = created_at;
    }

}
