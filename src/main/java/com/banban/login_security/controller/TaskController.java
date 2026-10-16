package com.banban.login_security.controller;

import com.banban.login_security.code.SuccessCode;
import com.banban.login_security.domain.Response;
import com.banban.login_security.domain.agent.AgentRequest;
import com.banban.login_security.domain.agent.AgentTask;
import com.banban.login_security.service.AgentTaskService;
import com.banban.login_security.type.TaskStatus;
import org.springframework.security.core.Authentication;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/tasks")
public class TaskController {
    private final AgentTaskService agentTaskService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> addTask(@RequestBody AgentTask task, Authentication authentication) {
        task.setUser_id(Long.parseLong(authentication.getName()));
        agentTaskService.addAgentTask(task);
        return Response.toResponseEntity(SuccessCode.OK, task);
    }

    @GetMapping
    public ResponseEntity<Response> findTasks(@RequestParam(required = false) Long agentId,
                                             Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        return Response.toResponseEntity(SuccessCode.OK, agentTaskService.findOwnedTasks(userId, agentId));
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<Response> findTask(@PathVariable Long taskId, Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        return Response.toResponseEntity(SuccessCode.OK, agentTaskService.findOwnedTask(userId, taskId));
    }

    @PatchMapping(value = "/{taskId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> updateTask(@PathVariable Long taskId, @RequestBody AgentTask task,
                                              Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        agentTaskService.updateTask(userId, taskId, task);
        return Response.toResponseEntity(SuccessCode.OK, null);
    }

    @PatchMapping(value = "/{taskId}/status", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> changeStatus(@PathVariable Long taskId, @RequestBody AgentTask task,
                                                Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        agentTaskService.changeTaskStatus(userId, taskId, task.getStatus());
        return Response.toResponseEntity(SuccessCode.OK, null);
    }

    @PatchMapping("/{taskId}/cancel")
    public ResponseEntity<Response> stopTask(@PathVariable Long taskId, Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        agentTaskService.changeTaskStatus(userId, taskId, TaskStatus.CANCELLED.name());
        return Response.toResponseEntity(SuccessCode.OK, null);
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Response> deleteTask(@PathVariable Long taskId, Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        agentTaskService.deleteTask(userId, taskId);
        return Response.toResponseEntity(SuccessCode.OK, null);
    }

    @PostMapping(value = "/authorization/check", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> authCheck(@RequestBody AgentRequest agentRequest){
        return Response.toResponseEntity(SuccessCode.OK, null);
    }
}
