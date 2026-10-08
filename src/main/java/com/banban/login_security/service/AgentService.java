package com.banban.login_security.service;

import com.banban.login_security.domain.agent.Agent;

import java.util.Optional;

public interface AgentService {
    public Optional<Agent> findAgentForId(Long id);
    public Optional<Agent> findAgentForUserIdAndName(Long id, String name);
    public void addAgent(Agent agent);
}
