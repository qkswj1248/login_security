package com.banban.login_security.service;

import com.banban.login_security.domain.agent.AgentPermission;

import java.util.List;

public interface AgentPermissionService {
    public List<AgentPermission> findByAgentId(Long agent_id);
    public List<AgentPermission> findByResource(Long agent_id, String resource, String action);
    void addAgentPermission(AgentPermission agentPermission);
    void addOwnedPermission(Long userId, Long agentId, AgentPermission permission);
    List<AgentPermission> findOwnedPermissions(Long userId, Long agentId);
    AgentPermission findOwnedPermission(Long userId, Long agentId, Long permissionId);
    void updatePermission(Long userId, Long agentId, Long permissionId, AgentPermission permission);
    void deletePermission(Long userId, Long agentId, Long permissionId);
}
