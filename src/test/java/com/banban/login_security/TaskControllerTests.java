package com.banban.login_security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:task_crud_test;MODE=MySQL;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
class TaskControllerTests {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private long createAgent() throws Exception {
        String response = mvc.perform(post("/agents").with(user("1"))
                        .contentType("application/json").content("{\"name\":\"작업 에이전트\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).path("detail").path("id").asLong();
    }

    private String taskBody(long agentId) {
        return "{\"agent_id\":" + agentId + ",\"purpose\":\"자료 검색\",\"expired_at\":\""
                + OffsetDateTime.now().plusHours(1) + "\",\"user_id\":2,\"id\":999,\"status\":\"COMPLETED\"}";
    }

    private long createTask(long agentId) throws Exception {
        String response = mvc.perform(post("/tasks").with(user("1"))
                        .contentType("application/json").content(taskBody(agentId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.detail.user_id").value(1))
                .andExpect(jsonPath("$.detail.agent_id").value(agentId))
                .andExpect(jsonPath("$.detail.status").value("RUNNING"))
                .andExpect(jsonPath("$.detail.created_at").exists())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).path("detail").path("id").asLong();
    }

    @Test
    void taskCrudAndEachWorkCreatesNewTask() throws Exception {
        long agentId = createAgent();
        long id = createTask(agentId);
        long nextId = createTask(agentId);
        assertNotEquals(id, nextId);
        mvc.perform(get("/tasks").param("agentId", Long.toString(agentId)).with(user("1")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.detail.length()").value(2));
        mvc.perform(get("/tasks/{id}", id).with(user("1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.detail.created_at").exists())
                .andExpect(jsonPath("$.detail.expired_at").exists());
        String body = "{\"purpose\":\"검색 범위 변경\",\"expired_at\":\"" + OffsetDateTime.now().plusHours(2)
                + "\",\"agent_id\":999,\"user_id\":2,\"status\":\"COMPLETED\"}";
        mvc.perform(patch("/tasks/{id}", id).with(user("1"))
                        .contentType("application/json").content(body))
                .andExpect(status().isOk());
        mvc.perform(get("/tasks/{id}", id).with(user("1")))
                .andExpect(jsonPath("$.detail.purpose").value("검색 범위 변경"))
                .andExpect(jsonPath("$.detail.agent_id").value(agentId))
                .andExpect(jsonPath("$.detail.user_id").value(1))
                .andExpect(jsonPath("$.detail.status").value("RUNNING"));
        mvc.perform(delete("/tasks/{id}", id).with(user("1"))).andExpect(status().isOk());
        mvc.perform(get("/tasks/{id}", id).with(user("1"))).andExpect(status().isNotFound());
        mvc.perform(get("/tasks/{id}", nextId).with(user("1"))).andExpect(status().isOk());
    }

    @Test
    void otherUserCannotCreateReadOrChangeTasks() throws Exception {
        long agentId = createAgent();
        long id = createTask(agentId);
        mvc.perform(post("/tasks").with(user("2"))
                        .contentType("application/json").content(taskBody(agentId)))
                .andExpect(status().isNotFound());
        mvc.perform(get("/tasks").with(user("2")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.detail").doesNotExist());
        mvc.perform(get("/tasks").param("agentId", Long.toString(agentId)).with(user("2")))
                .andExpect(status().isNotFound());
        mvc.perform(get("/tasks/{id}", id).with(user("2"))).andExpect(status().isNotFound());
        mvc.perform(patch("/tasks/{id}", id).with(user("2"))
                        .contentType("application/json").content(taskBody(agentId)))
                .andExpect(status().isNotFound());
        mvc.perform(patch("/tasks/{id}/status", id).with(user("2"))
                        .contentType("application/json").content("{\"status\":\"COMPLETED\"}"))
                .andExpect(status().isNotFound());
        mvc.perform(patch("/tasks/{id}/cancel", id).with(user("2"))).andExpect(status().isNotFound());
        mvc.perform(delete("/tasks/{id}", id).with(user("2"))).andExpect(status().isNotFound());
        mvc.perform(get("/tasks/{id}", id).with(user("1")))
                .andExpect(jsonPath("$.detail.status").value("RUNNING"));
    }

    @Test
    void completedCancelledAndExpiredTasksCannotBeChanged() throws Exception {
        long agentId = createAgent();
        long completed = createTask(agentId);
        long cancelled = createTask(agentId);
        long expired = createTask(agentId);
        mvc.perform(patch("/tasks/{id}/status", completed).with(user("1"))
                        .contentType("application/json").content("{\"status\":\"COMPLETED\"}"))
                .andExpect(status().isOk());
        mvc.perform(patch("/tasks/{id}/cancel", cancelled).with(user("1"))).andExpect(status().isOk());
        jdbcTemplate.update("update AGENT_TASK set EXPIRED_AT = ? where ID = ?", OffsetDateTime.now().minusHours(1), expired);
        for (long id : new long[]{completed, cancelled, expired}) {
            mvc.perform(patch("/tasks/{id}", id).with(user("1"))
                            .contentType("application/json").content(taskBody(agentId)))
                    .andExpect(status().isConflict());
            mvc.perform(patch("/tasks/{id}/cancel", id).with(user("1")))
                    .andExpect(status().isConflict());
        }
        mvc.perform(get("/tasks/{id}", completed).with(user("1")))
                .andExpect(jsonPath("$.detail.status").value("COMPLETED"));
        mvc.perform(get("/tasks/{id}", cancelled).with(user("1")))
                .andExpect(jsonPath("$.detail.status").value("CANCELLED"));
    }

    @Test
    void invalidInputAndInactiveAgentAreRejected() throws Exception {
        long agentId = createAgent();
        String valid = taskBody(agentId);
        ObjectNode expiredBody = (ObjectNode) objectMapper.readTree(valid);
        expiredBody.put("expired_at", "2000-01-01T00:00:00Z");
        for (String body : new String[]{"{}", valid.replace("자료 검색", " "),
                valid.replace("자료 검색", "x".repeat(501)), expiredBody.toString()}) {
            mvc.perform(post("/tasks").with(user("1")).contentType("application/json").content(body))
                    .andExpect(status().isBadRequest());
        }
        long id = createTask(agentId);
        mvc.perform(patch("/tasks/{id}/status", id).with(user("1"))
                        .contentType("application/json").content("{\"status\":\"RUNNING\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/tasks").with(user("1"))
                        .contentType("application/json").content(taskBody(Long.MAX_VALUE)))
                .andExpect(status().isNotFound());
        mvc.perform(get("/tasks/{id}", Long.MAX_VALUE).with(user("1"))).andExpect(status().isNotFound());
        mvc.perform(patch("/agents/{id}/status", agentId).with(user("1"))
                        .contentType("application/json").content("{\"status\":\"DISABLED\"}"))
                .andExpect(status().isOk());
        mvc.perform(post("/tasks").with(user("1")).contentType("application/json").content(valid))
                .andExpect(status().isConflict());
    }

    @Test
    void tasksRequireLogin() throws Exception {
        // 기존 인증 진입점은 인증되지 않은 요청에 HTTP 404를 반환한다.
        mvc.perform(get("/tasks"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.name").value("CUSTOM_EXPIRED_TOKEN"));
    }
}
