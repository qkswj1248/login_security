package com.banban.login_security.domain.agent;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 기존 AuthorizationLog를 변경하지 않기 위한 CRUD용 모델이다.
@Getter @Setter
@NoArgsConstructor
public class AuthorizationLogEntry {
    private Long id;
    private String principal_type;
    private Long principal_id;
    private Long task_id;
    private String resource;
    private String action;
    private String scope;
    private String result;
    private String reason;
}
