package com.banban.login_security.domain.agent;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AgentRequest {
    private Long agent_id;
    private Long task_id;
    private String resource;
    private String action;
    private String scope;
}
