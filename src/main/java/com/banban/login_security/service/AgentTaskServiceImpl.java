package com.banban.login_security.service;

import com.banban.login_security.code.TaskErrorCode;
import com.banban.login_security.domain.agent.Agent;
import com.banban.login_security.domain.agent.AgentTask;
import com.banban.login_security.error.CustomException;
import com.banban.login_security.mapper.AgentTaskMapper;
import com.banban.login_security.type.Status;
import com.banban.login_security.type.TaskStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AgentTaskServiceImpl implements AgentTaskService {
    private final AgentTaskMapper agentTaskMapper;
    private final AgentService agentService;

    @Override
    public Optional<AgentTask> findByTaskId(Long task_id) {
        return agentTaskMapper.findByTaskId(task_id);
    }

    @Override
    public List<AgentTask> findByAgentId(Long agent_id) {
        return agentTaskMapper.findByAgentId(agent_id);
    }

    @Override
    public void addAgentTask(AgentTask task) {
        checkTask(task);
        if (task.getAgent_id() == null) {
            throw new CustomException(TaskErrorCode.INVALID_TASK, "에이전트 ID가 필요합니다");
        }
        Agent agent = agentService.findOwnedAgent(task.getUser_id(), task.getAgent_id());
        if (!Status.ACTIVE.name().equals(agent.getStatus())) {
            throw new CustomException(TaskErrorCode.AGENT_NOT_ACTIVE, "비활성 에이전트입니다");
        }
        // 같은 에이전트라도 작업을 시작할 때마다 새 행을 저장한다.
        task.setId(null);
        task.setStatus(TaskStatus.RUNNING.name());
        task.setCreated_at(OffsetDateTime.now());
        agentTaskMapper.insert(task);
    }

    @Override
    public List<AgentTask> findOwnedTasks(Long userId, Long agentId) {
        if (agentId != null) {
            agentService.findOwnedAgent(userId, agentId);
        }
        return agentTaskMapper.findByUserId(userId, agentId);
    }

    @Override
    public AgentTask findOwnedTask(Long userId, Long taskId) {
        return agentTaskMapper.findByTaskId(taskId)
                .filter(task -> userId.equals(task.getUser_id()))
                .orElseThrow(() -> new CustomException(TaskErrorCode.TASK_NOT_FOUND, "작업을 찾을 수 없습니다"));
    }

    @Override
    public void updateTask(Long userId, Long taskId, AgentTask task) {
        checkRunning(findOwnedTask(userId, taskId));
        checkTask(task);
        // 소유자와 에이전트, 생성 시각은 변경하지 않는다.
        task.setId(taskId);
        if (agentTaskMapper.update(task, userId) != 1) {
            throw new CustomException(TaskErrorCode.TASK_NOT_RUNNING, "작업을 변경할 수 없습니다");
        }
    }

    @Override
    public void changeTaskStatus(Long userId, Long taskId, String status) {
        checkRunning(findOwnedTask(userId, taskId));
        if (!TaskStatus.COMPLETED.name().equals(status) && !TaskStatus.CANCELLED.name().equals(status)) {
            throw new CustomException(TaskErrorCode.INVALID_TASK_STATUS, "잘못된 작업 상태입니다");
        }
        if (agentTaskMapper.changeStatus(taskId, userId, status) != 1) {
            throw new CustomException(TaskErrorCode.TASK_NOT_RUNNING, "작업을 변경할 수 없습니다");
        }
    }

    @Override
    public void deleteTask(Long userId, Long taskId) {
        findOwnedTask(userId, taskId);
        agentTaskMapper.delete(taskId, userId);
    }

    private void checkTask(AgentTask task) {
        if (task.getPurpose() == null || task.getPurpose().isBlank() || task.getPurpose().length() > 500
                || task.getExpired_at() == null || !task.getExpired_at().isAfter(OffsetDateTime.now())) {
            throw new CustomException(TaskErrorCode.INVALID_TASK, "작업 목적과 미래의 만료 시각이 필요합니다");
        }
    }

    private void checkRunning(AgentTask task) {
        if (!TaskStatus.RUNNING.name().equals(task.getStatus())
                || !task.getExpired_at().isAfter(OffsetDateTime.now())) {
            throw new CustomException(TaskErrorCode.TASK_NOT_RUNNING, "종료되거나 만료된 작업입니다");
        }
    }
}
