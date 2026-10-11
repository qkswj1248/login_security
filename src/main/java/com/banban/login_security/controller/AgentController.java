package com.banban.login_security.controller;

import com.banban.login_security.code.SuccessCode;
import com.banban.login_security.domain.Response;
import com.banban.login_security.domain.agent.Agent;
import com.banban.login_security.domain.agent.AgentPermission;
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
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> addAgent(@RequestBody Agent agent){

        return Response.toResponseEntity(SuccessCode.AGENT_CREATE_SUCCESS, null);
    }

    @GetMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> findAgents(){
        return Response.toResponseEntity(SuccessCode.OK, null);
    }

    @PatchMapping(value = "/{id}/status", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> changeStatus(@PathVariable String id){
        return Response.toResponseEntity(SuccessCode.OK, null);
    }

    @PostMapping(value = "/{id}/permissions", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> addPermission(@PathVariable String id, @RequestBody AgentPermission agentPermission){
        return Response.toResponseEntity(SuccessCode.OK, null);
    }

    @GetMapping(value = "/{id}/permissions", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> findPermissions(@PathVariable String id){
        return Response.toResponseEntity(SuccessCode.OK, null);
    }

    @DeleteMapping(value = "/{id}/permissions/{permissionId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> deletePermission(@PathVariable String id, @PathVariable String permissionId){
        return Response.toResponseEntity(SuccessCode.OK, null);
    }
}
