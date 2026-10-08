package com.banban.login_security.domain.agent;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter @Setter
public class AgentTask {
    private Long id;
    private Long user_id;
    private Long agent_id;
    private String status;
    private String purpose;
    private OffsetDateTime created_at;
    private OffsetDateTime expired_at;

    public AgentTask(Long id, Long user_id, Long agent_id, String status, String purpose, OffsetDateTime created_at, OffsetDateTime expired_at){
        this.id = id;
        this.user_id = user_id;
        this.agent_id = agent_id;
        this.status = status;
        this.purpose = purpose;
        this.created_at = created_at;
        this.expired_at = expired_at;
    }

}
