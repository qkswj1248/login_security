package com.banban.login_security.controller;

import com.banban.login_security.code.SuccessCode;
import com.banban.login_security.domain.Response;
import com.banban.login_security.domain.agent.Agent;
import com.banban.login_security.domain.agent.AgentPermission;
import com.banban.login_security.service.AgentService;
import com.banban.login_security.service.AgentPermissionService;
import org.springframework.security.core.Authentication;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/agents")
public class AgentController {
    private final AgentService agentService;
    private final AgentPermissionService agentPermissionService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> addAgent(@RequestBody Agent agent, Authentication authentication) {
        // 요청 본문의 user_id 대신 로그인한 사용자의 ID를 사용한다.
        agent.setUser_id(Long.parseLong(authentication.getName()));
        agentService.addAgent(agent);
        return Response.toResponseEntity(SuccessCode.AGENT_CREATE_SUCCESS, agent);
    }

    @GetMapping
    public ResponseEntity<Response> findAgents(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        return Response.toResponseEntity(SuccessCode.OK, agentService.findAgentsForUserId(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response> findAgent(@PathVariable Long id, Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        return Response.toResponseEntity(SuccessCode.OK, agentService.findOwnedAgent(userId, id));
    }

    @PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> changeName(@PathVariable Long id, @RequestBody Agent agent,
                                              Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        agentService.changeAgentName(userId, id, agent.getName());
        return Response.toResponseEntity(SuccessCode.OK, null);
    }

    @PatchMapping(value = "/{id}/status", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> changeStatus(@PathVariable Long id, @RequestBody Agent agent,
                                                Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        agentService.changeAgentStatus(userId, id, agent.getStatus());
        return Response.toResponseEntity(SuccessCode.OK, null);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Response> deleteAgent(@PathVariable Long id, Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        agentService.deleteAgent(userId, id);
        return Response.toResponseEntity(SuccessCode.OK, null);
    }

    @PostMapping(value = "/{id}/permissions", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> addPermission(@PathVariable Long id, @RequestBody AgentPermission permission,
                                                 Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        agentPermissionService.addOwnedPermission(userId, id, permission);
        return Response.toResponseEntity(SuccessCode.OK, permission);
    }

    @GetMapping("/{id}/permissions")
    public ResponseEntity<Response> findPermissions(@PathVariable Long id, Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        return Response.toResponseEntity(SuccessCode.OK, agentPermissionService.findOwnedPermissions(userId, id));
    }

    @GetMapping("/{id}/permissions/{permissionId}")
    public ResponseEntity<Response> findPermission(@PathVariable Long id, @PathVariable Long permissionId,
                                                  Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        return Response.toResponseEntity(SuccessCode.OK,
                agentPermissionService.findOwnedPermission(userId, id, permissionId));
    }

    @PatchMapping(value = "/{id}/permissions/{permissionId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> updatePermission(@PathVariable Long id, @PathVariable Long permissionId,
                                                    @RequestBody AgentPermission permission, Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        agentPermissionService.updatePermission(userId, id, permissionId, permission);
        return Response.toResponseEntity(SuccessCode.OK, null);
    }

    @DeleteMapping("/{id}/permissions/{permissionId}")
    public ResponseEntity<Response> deletePermission(@PathVariable Long id, @PathVariable Long permissionId,
                                                    Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        agentPermissionService.deletePermission(userId, id, permissionId);
        return Response.toResponseEntity(SuccessCode.OK, null);
    }
}
