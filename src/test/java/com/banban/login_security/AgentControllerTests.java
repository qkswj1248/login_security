package com.banban.login_security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:agent_crud_test;MODE=MySQL;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
class AgentControllerTests {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    private long createAgent() throws Exception {
        String response = mvc.perform(post("/agents").with(user("1"))
                        .contentType("application/json")
                        .content("{\"name\":\"도우미\",\"user_id\":2,\"status\":\"DISABLED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.detail.user_id").value(1))
                .andExpect(jsonPath("$.detail.status").value("ACTIVE"))
                .andExpect(jsonPath("$.detail.created_at").exists())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).path("detail").path("id").asLong();
    }

    @Test
    void agentCrud() throws Exception {
        long id = createAgent();
        mvc.perform(get("/agents").with(user("1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.detail[0].id").value(id));
        mvc.perform(get("/agents/{id}", id).with(user("1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.detail.user_id").value(1))
                .andExpect(jsonPath("$.detail.created_at").exists());
        mvc.perform(patch("/agents/{id}", id).with(user("1"))
                        .contentType("application/json").content("{\"name\":\"새 이름\"}"))
                .andExpect(status().isOk());
        mvc.perform(patch("/agents/{id}/status", id).with(user("1"))
                        .contentType("application/json").content("{\"status\":\"DISABLED\"}"))
                .andExpect(status().isOk());
        mvc.perform(get("/agents/{id}", id).with(user("1")))
                .andExpect(jsonPath("$.detail.name").value("새 이름"))
                .andExpect(jsonPath("$.detail.status").value("DISABLED"));
        mvc.perform(delete("/agents/{id}", id).with(user("1")))
                .andExpect(status().isOk());
        mvc.perform(get("/agents/{id}", id).with(user("1")))
                .andExpect(status().isNotFound());
    }

    @Test
    void otherUserCannotManageAgent() throws Exception {
        long id = createAgent();
        mvc.perform(get("/agents").with(user("2")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.detail").doesNotExist());
        mvc.perform(get("/agents/{id}", id).with(user("2")))
                .andExpect(status().isNotFound());
        mvc.perform(patch("/agents/{id}", id).with(user("2"))
                        .contentType("application/json").content("{\"name\":\"다른 이름\"}"))
                .andExpect(status().isNotFound());
        mvc.perform(patch("/agents/{id}/status", id).with(user("2"))
                        .contentType("application/json").content("{\"status\":\"DISABLED\"}"))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/agents/{id}", id).with(user("2")))
                .andExpect(status().isNotFound());
        mvc.perform(get("/agents/{id}", id).with(user("1")))
                .andExpect(jsonPath("$.detail.status").value("ACTIVE"));
    }

    @Test
    void invalidNameAndStatusAreRejected() throws Exception {
        mvc.perform(post("/agents").with(user("1"))
                        .contentType("application/json").content("{\"name\":\" \"}"))
                .andExpect(status().isBadRequest());
        long id = createAgent();
        mvc.perform(patch("/agents/{id}", id).with(user("1"))
                        .contentType("application/json").content("{}"))
                .andExpect(status().isBadRequest());
        mvc.perform(patch("/agents/{id}/status", id).with(user("1"))
                        .contentType("application/json").content("{\"status\":\"UNKNOWN\"}"))
                .andExpect(status().isBadRequest());
    }
}
