package com.banban.login_security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:permission_crud_test;MODE=MySQL;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
class AgentPermissionControllerTests {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;

    private long createAgent() throws Exception {
        String response = mvc.perform(post("/agents").with(user("1"))
                        .contentType("application/json").content("{\"name\":\"도우미\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).path("detail").path("id").asLong();
    }

    private JsonNode createPermission(long agentId) throws Exception {
        String response = mvc.perform(post("/agents/{id}/permissions", agentId).with(user("1"))
                        .contentType("application/json")
                        .content("{\"id\":999,\"agent_id\":999,\"resource\":\"document\",\"action\":\"read\",\"scope\":\"mine\",\"created_at\":\"2000-01-01T00:00:00Z\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.detail.agent_id").value(agentId))
                .andExpect(jsonPath("$.detail.created_at").exists())
                .andReturn().getResponse().getContentAsString();
        JsonNode permission = objectMapper.readTree(response).path("detail");
        assertNotEquals(999, permission.path("id").asLong());
        assertNotEquals("2000-01-01T00:00:00Z", permission.path("created_at").asText());
        return permission;
    }

    @Test
    void crudAndRepeatedRequestsAreSeparateRecords() throws Exception {
        long agentId = createAgent();
        JsonNode first = createPermission(agentId);
        long id = first.path("id").asLong();
        long secondId = createPermission(agentId).path("id").asLong();
        assertNotEquals(id, secondId);
        mvc.perform(get("/agents/{id}/permissions", agentId).with(user("1")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.detail.length()").value(2));
        String saved = mvc.perform(get("/agents/{id}/permissions/{permissionId}", agentId, id).with(user("1")))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String createdAt = objectMapper.readTree(saved).path("detail").path("created_at").asText();
        mvc.perform(patch("/agents/{id}/permissions/{permissionId}", agentId, id).with(user("1"))
                        .contentType("application/json")
                        .content("{\"resource\":\"file\",\"action\":\"write\",\"scope\":\"shared\",\"agent_id\":999,\"created_at\":\"2000-01-01T00:00:00Z\"}"))
                .andExpect(status().isOk());
        mvc.perform(get("/agents/{id}/permissions/{permissionId}", agentId, id).with(user("1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.detail.resource").value("file"))
                .andExpect(jsonPath("$.detail.action").value("write"))
                .andExpect(jsonPath("$.detail.scope").value("shared"))
                .andExpect(jsonPath("$.detail.agent_id").value(agentId))
                .andExpect(jsonPath("$.detail.created_at").value(createdAt));
        mvc.perform(delete("/agents/{id}/permissions/{permissionId}", agentId, id).with(user("1")))
                .andExpect(status().isOk());
        mvc.perform(get("/agents/{id}/permissions/{permissionId}", agentId, id).with(user("1")))
                .andExpect(status().isNotFound());
        mvc.perform(get("/agents/{id}/permissions/{permissionId}", agentId, secondId).with(user("1")))
                .andExpect(status().isOk());
    }

    @Test
    void otherUserAndWrongAgentCannotAccessRecords() throws Exception {
        long agentId = createAgent();
        long id = createPermission(agentId).path("id").asLong();
        String body = "{\"resource\":\"file\",\"action\":\"read\"}";
        mvc.perform(post("/agents/{id}/permissions", agentId).with(user("2"))
                        .contentType("application/json").content(body)).andExpect(status().isNotFound());
        mvc.perform(get("/agents/{id}/permissions", agentId).with(user("2"))).andExpect(status().isNotFound());
        mvc.perform(get("/agents/{id}/permissions/{permissionId}", agentId, id).with(user("2")))
                .andExpect(status().isNotFound());
        mvc.perform(patch("/agents/{id}/permissions/{permissionId}", agentId, id).with(user("2"))
                        .contentType("application/json").content(body)).andExpect(status().isNotFound());
        mvc.perform(delete("/agents/{id}/permissions/{permissionId}", agentId, id).with(user("2")))
                .andExpect(status().isNotFound());
        long otherAgent = createAgent();
        mvc.perform(get("/agents/{id}/permissions/{permissionId}", otherAgent, id).with(user("1")))
                .andExpect(status().isNotFound());
        mvc.perform(patch("/agents/{id}/permissions/{permissionId}", otherAgent, id).with(user("1"))
                        .contentType("application/json").content(body)).andExpect(status().isNotFound());
        mvc.perform(delete("/agents/{id}/permissions/{permissionId}", otherAgent, id).with(user("1")))
                .andExpect(status().isNotFound());
        mvc.perform(get("/agents/{id}/permissions/{permissionId}", agentId, id).with(user("1")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.detail.resource").value("document"));
    }

    @Test
    void invalidInputAndMissingRecordsAreRejected() throws Exception {
        long agentId = createAgent();
        long id = createPermission(agentId).path("id").asLong();
        for (String body : new String[]{"{}", "{\"resource\":\" \",\"action\":\"read\"}",
                "{\"resource\":\"file\",\"action\":\"\"}",
                "{\"resource\":\"" + "x".repeat(256) + "\",\"action\":\"read\"}",
                "{\"resource\":\"file\",\"action\":\"" + "x".repeat(256) + "\"}",
                "{\"resource\":\"file\",\"action\":\"read\",\"scope\":\"" + "x".repeat(256) + "\"}"}) {
            mvc.perform(post("/agents/{id}/permissions", agentId).with(user("1"))
                            .contentType("application/json").content(body)).andExpect(status().isBadRequest());
            mvc.perform(patch("/agents/{id}/permissions/{permissionId}", agentId, id).with(user("1"))
                            .contentType("application/json").content(body)).andExpect(status().isBadRequest());
        }
        mvc.perform(post("/agents/{id}/permissions", agentId).with(user("1"))
                        .contentType("application/json").content("{\"resource\":\"file\",\"action\":\"read\"}"))
                .andExpect(status().isOk());
        mvc.perform(get("/agents/{id}/permissions/{permissionId}", agentId, Long.MAX_VALUE).with(user("1")))
                .andExpect(status().isNotFound());
        mvc.perform(get("/agents/{id}/permissions", Long.MAX_VALUE).with(user("1")))
                .andExpect(status().isNotFound());
    }

    @Test
    void loginIsRequired() throws Exception {
        mvc.perform(get("/agents/1/permissions"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.name").value("CUSTOM_EXPIRED_TOKEN"));
    }
}
