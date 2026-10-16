package com.banban.login_security.service;

import com.banban.login_security.domain.agent.AgentTask;

import java.util.List;
import java.util.Optional;

public interface AgentTaskService {
    public Optional<AgentTask> findByTaskId(Long task_id);
    public List<AgentTask> findByAgentId(Long agent_id);
    public void addAgentTask(AgentTask agentTask);
    List<AgentTask> findOwnedTasks(Long userId, Long agentId);
    AgentTask findOwnedTask(Long userId, Long taskId);
    void updateTask(Long userId, Long taskId, AgentTask task);
    void changeTaskStatus(Long userId, Long taskId, String status);
    void deleteTask(Long userId, Long taskId);
}
