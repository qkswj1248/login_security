package com.banban.login_security.mapper;

import com.banban.login_security.domain.agent.AgentTask;

import java.util.List;
import java.util.Optional;

public interface AgentTaskMapper {
    void insert(AgentTask agentTask);
    Optional<AgentTask> findByTaskId(Long task_id);
    List<AgentTask> findByAgentId(Long agent_id);
}
