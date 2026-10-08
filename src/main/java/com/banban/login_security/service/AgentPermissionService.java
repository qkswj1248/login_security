package com.banban.login_security.service;

import com.banban.login_security.domain.agent.AgentPermission;

import java.util.List;
import java.util.Optional;

public interface AgentPermissionService {
    public List<AgentPermission> findByAgentId(Long agent_id);
    public List<AgentPermission> findByResource(Long agent_id, String resource, String action);
    void addAgentPermission(AgentPermission agentPermission);
}
