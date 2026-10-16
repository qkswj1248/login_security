package com.banban.login_security.service;

import com.banban.login_security.code.AuthorizationLogErrorCode;
import com.banban.login_security.domain.agent.AgentTask;
import com.banban.login_security.domain.agent.AuthorizationLog;
import com.banban.login_security.error.CustomException;
import com.banban.login_security.mapper.AuthorizationLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthorizationLogServiceImpl {
    private final AuthorizationLogMapper mapper;
    private final AgentTaskService taskService;

    public void addLog(Long userId, Long taskId, AuthorizationLog log) {
        AgentTask task = taskService.findOwnedTask(userId, taskId);
        checkLog(log);
        // 주체와 Task는 요청 본문을 신뢰하지 않고 서버에서 결정한다.
        log.setId(null);
        log.setTask_id(taskId);
        log.setPrincipal_type("AGENT");
        log.setPrincipal_id(task.getAgent_id());
        mapper.insert(log);
    }

    private void checkLog(AuthorizationLog log) {
        if (log.getResource() == null || log.getResource().isBlank() || log.getResource().length() > 255
                || log.getAction() == null || log.getAction().isBlank() || log.getAction().length() > 255
                || (log.getScope() != null && log.getScope().length() > 255)
                || (log.getReason() != null && log.getReason().length() > 500)
                || (log.getResult() != null && !"ALLOW".equals(log.getResult()) && !"DENY".equals(log.getResult()))) {
            throw new CustomException(AuthorizationLogErrorCode.INVALID_LOG, "잘못된 로그 내용입니다");
        }
    }
}
