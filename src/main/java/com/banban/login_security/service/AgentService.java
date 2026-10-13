package com.banban.login_security.service;

import com.banban.login_security.domain.agent.Agent;

import java.util.Optional;
import java.util.List;

public interface AgentService {
    public Optional<Agent> findAgentForId(Long id);
    public Optional<Agent> findAgentForUserIdAndName(Long id, String name);
    public void addAgent(Agent agent);
    List<Agent> findAgentsForUserId(Long userId);
    Agent findOwnedAgent(Long userId, Long id);
    void changeAgentName(Long userId, Long id, String name);
    void changeAgentStatus(Long userId, Long id, String status);
    void deleteAgent(Long userId, Long id);
}
