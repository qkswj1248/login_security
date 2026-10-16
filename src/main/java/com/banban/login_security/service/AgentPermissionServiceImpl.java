package com.banban.login_security.service;

import com.banban.login_security.code.AgentPermissionErrorCode;
import com.banban.login_security.domain.agent.AgentPermission;
import com.banban.login_security.error.CustomException;
import com.banban.login_security.mapper.AgentPermissionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AgentPermissionServiceImpl implements AgentPermissionService {
    private final AgentPermissionMapper agentPermissionMapper;
    private final AgentService agentService;

    @Override
    public List<AgentPermission> findByAgentId(Long agent_id) {
        return agentPermissionMapper.findByAgentId(agent_id);
    }

    @Override
    public List<AgentPermission> findByResource(Long agent_id, String resource, String action) {
        return agentPermissionMapper.findByResource(agent_id, resource, action);
    }

    @Override
    public void addAgentPermission(AgentPermission permission) {
        checkPermission(permission);
        // 동일한 요청도 새 기록으로 저장한다. 승인 여부는 판단하지 않는다.
        permission.setId(null);
        permission.setCreated_at(OffsetDateTime.now());
        agentPermissionMapper.insert(permission);
    }

    @Override
    public void addOwnedPermission(Long userId, Long agentId, AgentPermission permission) {
        agentService.findOwnedAgent(userId, agentId);
        permission.setAgent_id(agentId);
        addAgentPermission(permission);
    }

    @Override
    public List<AgentPermission> findOwnedPermissions(Long userId, Long agentId) {
        agentService.findOwnedAgent(userId, agentId);
        return findByAgentId(agentId);
    }

    @Override
    public AgentPermission findOwnedPermission(Long userId, Long agentId, Long permissionId) {
        agentService.findOwnedAgent(userId, agentId);
        // URL의 에이전트에 속한 기록인지 함께 확인한다.
        return agentPermissionMapper.findById(agentId, permissionId)
                .orElseThrow(() -> new CustomException(
                        AgentPermissionErrorCode.PERMISSION_NOT_FOUND, "권한 요청 기록을 찾을 수 없습니다"));
    }

    @Override
    public void updatePermission(Long userId, Long agentId, Long permissionId, AgentPermission permission) {
        findOwnedPermission(userId, agentId, permissionId);
        checkPermission(permission);
        permission.setId(permissionId);
        permission.setAgent_id(agentId);
        // 생성 시각은 유지하고 요청 내용만 수정한다.
        agentPermissionMapper.update(permission);
    }

    @Override
    public void deletePermission(Long userId, Long agentId, Long permissionId) {
        findOwnedPermission(userId, agentId, permissionId);
        agentPermissionMapper.delete(agentId, permissionId);
    }

    private void checkPermission(AgentPermission permission) {
        if (permission.getResource() == null || permission.getResource().isBlank() || permission.getResource().length() > 255
                || permission.getAction() == null || permission.getAction().isBlank() || permission.getAction().length() > 255
                || (permission.getScope() != null && permission.getScope().length() > 255)) {
            throw new CustomException(AgentPermissionErrorCode.INVALID_PERMISSION, "잘못된 권한 요청 내용입니다");
        }
    }
}
