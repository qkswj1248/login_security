package com.banban.login_security.mapper;

import com.banban.login_security.domain.agent.Agent;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

public interface AgentMapper {
    Optional<Agent> findByUserIdAndName(@Param("user_id")Long user_id, @Param("name")String name);
    Optional<Agent> findById(Long id);
    void insert(Agent agent);
    void changeAgentStatus(@Param("user_id")Long id, @Param("status")String status);
}
