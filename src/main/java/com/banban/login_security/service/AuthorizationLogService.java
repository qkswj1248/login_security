package com.banban.login_security.service;

import com.banban.login_security.domain.agent.AuthorizationLog;

public interface AuthorizationLogService {
    void addAuthorizationLog(AuthorizationLog authorizationLog);
}
