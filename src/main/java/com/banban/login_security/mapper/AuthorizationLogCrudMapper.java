package com.banban.login_security.mapper;

import com.banban.login_security.domain.agent.AuthorizationLogEntry;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface AuthorizationLogCrudMapper {
    void insert(AuthorizationLogEntry log);
    List<AuthorizationLogEntry> findByTaskId(@Param("task_id") Long taskId, @Param("agent_id") Long agentId);
    Optional<AuthorizationLogEntry> findById(@Param("task_id") Long taskId, @Param("agent_id") Long agentId,
                                           @Param("id") Long id);
    void update(AuthorizationLogEntry log);
    void delete(@Param("task_id") Long taskId, @Param("agent_id") Long agentId, @Param("id") Long id);
}
