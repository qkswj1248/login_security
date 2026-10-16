package com.banban.login_security.domain.agent;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AuthorizationLog {
    private Long id;
    private String principal_type;
    private Long principal_id;
    private Long task_id;
    private String resource;
    private String action;
    private String scope;
    private String result;
    private String reason;

    public AuthorizationLog(Long id, String principal_type, Long principal_id, Long task_id, String resource, String action, String scope, String result, String reason){
        this.id = id;
        this.principal_type = principal_type;
        this.principal_id = principal_id;
        this.task_id = task_id;
        this.resource = resource;
        this.action = action;
        this.scope = scope;
        this.result = result;
        this.reason = reason;
    }
}
