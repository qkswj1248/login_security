package com.banban.login_security.controller;

import com.banban.login_security.code.SuccessCode;
import com.banban.login_security.domain.Response;
import com.banban.login_security.domain.agent.AgentRequest;
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
    @PatchMapping(value = "/{taskId}/cancel", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> stopTask(@PathVariable String taskId){
        return Response.toResponseEntity(SuccessCode.OK, null);
    }

    @GetMapping(value = "/{taskId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> findTasks(@PathVariable String taskId){
        return Response.toResponseEntity(SuccessCode.OK, null);
    }

    @PostMapping(value = "/authorization/check", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> authCheck(@RequestBody AgentRequest agentRequest){
        return Response.toResponseEntity(SuccessCode.OK, null);
    }
}
