package com.banban.login_security.mapper;

import com.banban.login_security.domain.agent.Agent;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;
import java.util.List;

@Mapper
public interface AgentMapper {
    Optional<Agent> findByUserIdAndName(@Param("user_id")Long user_id, @Param("name")String name);
    Optional<Agent> findById(Long id);
    void insert(Agent agent);
    void changeAgentStatus(@Param("id")Long id, @Param("status")String status);
    List<Agent> findByUserId(Long userId);
    void changeAgentName(@Param("id") Long id, @Param("user_id") Long userId, @Param("name") String name);
    void changeOwnedAgentStatus(@Param("id") Long id, @Param("user_id") Long userId, @Param("status") String status);
    void delete(@Param("id") Long id, @Param("user_id") Long userId);
}
