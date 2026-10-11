package com.banban.login_security.service;

import com.banban.login_security.domain.agent.Agent;
import org.springframework.security.core.parameters.P;

import java.util.Optional;

public class AgentServiceImpl implements AgentService{
    @Override
    public Optional<Agent> findAgentForId(Long id) {
        return Optional.empty();
    }

    @Override
    public Optional<Agent> findAgentForUserIdAndName(Long id, String name) {
        return Optional.empty();
    }

    @Override
    public void addAgent(Agent agent) {

    }
}
