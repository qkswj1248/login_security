package com.banban.login_security.service;

import com.banban.login_security.code.AgentErrorCode;
import com.banban.login_security.domain.agent.Agent;
import com.banban.login_security.error.CustomException;
import com.banban.login_security.mapper.AgentMapper;
import com.banban.login_security.type.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AgentServiceImpl implements AgentService {
    private final AgentMapper agentMapper;

    @Override
    public Optional<Agent> findAgentForId(Long id) {
        return agentMapper.findById(id);
    }

    @Override
    public Optional<Agent> findAgentForUserIdAndName(Long id, String name) {
        return agentMapper.findByUserIdAndName(id, name);
    }

    @Override
    public void addAgent(Agent agent) {
        checkName(agent.getName());
        // 등록 시 ID, 상태, 생성 시간은 서버에서 결정한다.
        agent.setId(null);
        agent.setStatus(Status.ACTIVE.name());
        agent.setCreated_at(OffsetDateTime.now());
        agentMapper.insert(agent);
    }

    @Override
    public List<Agent> findAgentsForUserId(Long userId) {
        return agentMapper.findByUserId(userId);
    }

    @Override
    public Agent findOwnedAgent(Long userId, Long id) {
        // 다른 사용자의 에이전트도 없는 에이전트와 동일하게 처리한다.
        return agentMapper.findById(id)
                .filter(agent -> userId.equals(agent.getUser_id()))
                .orElseThrow(() -> new CustomException(
                        AgentErrorCode.AGENT_NOT_FOUND, "에이전트를 찾을 수 없습니다"));
    }

    @Override
    public void changeAgentName(Long userId, Long id, String name) {
        findOwnedAgent(userId, id);
        checkName(name);
        agentMapper.changeAgentName(id, userId, name);
    }

    @Override
    public void changeAgentStatus(Long userId, Long id, String status) {
        findOwnedAgent(userId, id);
        if (!Status.ACTIVE.name().equals(status) && !Status.DISABLED.name().equals(status)) {
            throw new CustomException(AgentErrorCode.INVALID_AGENT_STATUS, "잘못된 상태입니다");
        }
        agentMapper.changeOwnedAgentStatus(id, userId, status);
    }

    @Override
    public void deleteAgent(Long userId, Long id) {
        findOwnedAgent(userId, id);
        agentMapper.delete(id, userId);
    }

    private void checkName(String name) {
        if (name == null || name.isBlank() || name.length() > 255) {
            throw new CustomException(AgentErrorCode.INVALID_AGENT_NAME, "잘못된 이름입니다");
        }
    }
}
