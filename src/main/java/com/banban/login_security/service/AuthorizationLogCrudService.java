package com.banban.login_security.service;

import com.banban.login_security.code.AuthorizationLogErrorCode;
import com.banban.login_security.domain.agent.AgentTask;
import com.banban.login_security.domain.agent.AuthorizationLogEntry;
import com.banban.login_security.error.CustomException;
import com.banban.login_security.mapper.AuthorizationLogCrudMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthorizationLogCrudService {
    private final AuthorizationLogCrudMapper mapper;
    private final AgentTaskService taskService;

    public AuthorizationLogEntry addLog(Long userId, Long taskId, AuthorizationLogEntry log) {
        AgentTask task = taskService.findOwnedTask(userId, taskId);
        checkLog(log);
        // 주체와 Task는 요청 본문을 신뢰하지 않고 서버에서 결정한다.
        log.setId(null);
        log.setTask_id(taskId);
        log.setPrincipal_type("AGENT");
        log.setPrincipal_id(task.getAgent_id());
        mapper.insert(log);
        return log;
    }

    public List<AuthorizationLogEntry> findLogs(Long userId, Long taskId) {
        AgentTask task = taskService.findOwnedTask(userId, taskId);
        return mapper.findByTaskId(taskId, task.getAgent_id());
    }

    public AuthorizationLogEntry findLog(Long userId, Long taskId, Long logId) {
        AgentTask task = taskService.findOwnedTask(userId, taskId);
        return mapper.findById(taskId, task.getAgent_id(), logId)
                .orElseThrow(() -> new CustomException(AuthorizationLogErrorCode.LOG_NOT_FOUND, "로그를 찾을 수 없습니다"));
    }

    public void updateLog(Long userId, Long taskId, Long logId, AuthorizationLogEntry log) {
        AuthorizationLogEntry saved = findLog(userId, taskId, logId);
        checkLog(log);
        log.setId(saved.getId());
        log.setTask_id(saved.getTask_id());
        log.setPrincipal_type(saved.getPrincipal_type());
        log.setPrincipal_id(saved.getPrincipal_id());
        mapper.update(log);
    }

    public void deleteLog(Long userId, Long taskId, Long logId) {
        AuthorizationLogEntry saved = findLog(userId, taskId, logId);
        mapper.delete(taskId, saved.getPrincipal_id(), logId);
    }

    private void checkLog(AuthorizationLogEntry log) {
        if (log.getResource() == null || log.getResource().isBlank() || log.getResource().length() > 255
                || log.getAction() == null || log.getAction().isBlank() || log.getAction().length() > 255
                || (log.getScope() != null && log.getScope().length() > 255)
                || (log.getReason() != null && log.getReason().length() > 500)
                || (log.getResult() != null && !"ALLOW".equals(log.getResult()) && !"DENY".equals(log.getResult()))) {
            throw new CustomException(AuthorizationLogErrorCode.INVALID_LOG, "잘못된 로그 내용입니다");
        }
    }
}
