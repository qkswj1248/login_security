package com.banban.login_security.mapper;

import com.banban.login_security.domain.agent.AuthorizationLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuthorizationLogMapper {
    void insert(AuthorizationLog authorizationLog);
}
