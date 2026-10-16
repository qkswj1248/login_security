package com.banban.login_security.controller;

import com.banban.login_security.code.SuccessCode;
import com.banban.login_security.domain.Response;
import com.banban.login_security.domain.agent.AuthorizationLogEntry;
import com.banban.login_security.service.AuthorizationLogCrudService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tasks/{taskId}/authorization-logs")
public class AuthorizationLogController {
    private final AuthorizationLogCrudService service;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> addLog(@PathVariable Long taskId, @RequestBody AuthorizationLogEntry log,
                                          Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        return Response.toResponseEntity(SuccessCode.OK, service.addLog(userId, taskId, log));
    }

    @GetMapping
    public ResponseEntity<Response> findLogs(@PathVariable Long taskId, Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        return Response.toResponseEntity(SuccessCode.OK, service.findLogs(userId, taskId));
    }

    @GetMapping("/{logId}")
    public ResponseEntity<Response> findLog(@PathVariable Long taskId, @PathVariable Long logId,
                                           Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        return Response.toResponseEntity(SuccessCode.OK, service.findLog(userId, taskId, logId));
    }

    @PatchMapping(value = "/{logId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> updateLog(@PathVariable Long taskId, @PathVariable Long logId,
                                             @RequestBody AuthorizationLogEntry log, Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        service.updateLog(userId, taskId, logId, log);
        return Response.toResponseEntity(SuccessCode.OK, null);
    }

    @DeleteMapping("/{logId}")
    public ResponseEntity<Response> deleteLog(@PathVariable Long taskId, @PathVariable Long logId,
                                             Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        service.deleteLog(userId, taskId, logId);
        return Response.toResponseEntity(SuccessCode.OK, null);
    }
}
