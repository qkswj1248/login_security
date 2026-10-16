package com.banban.login_security.mapper;

import com.banban.login_security.domain.agent.AgentTask;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface AgentTaskMapper {
    void insert(AgentTask agentTask);
    Optional<AgentTask> findByTaskId(Long task_id);
    List<AgentTask> findByAgentId(Long agent_id);
    List<AgentTask> findByUserId(@Param("user_id") Long userId, @Param("agent_id") Long agentId);
    int update(@Param("task") AgentTask task, @Param("user_id") Long userId);
    int changeStatus(@Param("id") Long id, @Param("user_id") Long userId, @Param("status") String status);
    void delete(@Param("id") Long id, @Param("user_id") Long userId);
}
