package com.banban.login_security;

import com.banban.login_security.config.AuthorizationLogSchemaConfig;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:authorization_log_test;MODE=MySQL;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
class AuthorizationLogControllerTests {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    @Qualifier("authorizationLogIdColumn")
    private InitializingBean schemaInitializer;

    private long createTask() throws Exception {
        String agent = mvc.perform(post("/agents").with(user("1"))
                        .contentType("application/json").content("{\"name\":\"로그용 에이전트\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        long agentId = objectMapper.readTree(agent).path("detail").path("id").asLong();
        String task = mvc.perform(post("/tasks").with(user("1"))
                        .contentType("application/json")
                        .content("{\"agent_id\":" + agentId + ",\"purpose\":\"로그 검증\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(task).path("detail").path("id").asLong();
    }

    private long createLog(long taskId) throws Exception {
        String response = mvc.perform(post("/tasks/{taskId}/authorization-logs", taskId).with(user("1"))
                        .contentType("application/json")
                        .content("{\"id\":999,\"task_id\":999,\"principal_type\":\"USER\",\"principal_id\":999,\"resource\":\"document\",\"action\":\"read\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.detail.task_id").value(taskId))
                .andExpect(jsonPath("$.detail.principal_type").value("AGENT"))
                .andReturn().getResponse().getContentAsString();
        JsonNode log = objectMapper.readTree(response).path("detail");
        Long agentId = jdbcTemplate.queryForObject("select AGENT_ID from AGENT_TASK where ID = ?", Long.class, taskId);
        assertEquals(agentId.longValue(), log.path("principal_id").asLong());
        assertNotEquals(999, log.path("id").asLong());
        return log.path("id").asLong();
    }

    @Test
    void crudAndRepeatedRequestsHaveDifferentIds() throws Exception {
        long taskId = createTask();
        long id = createLog(taskId);
        long otherId = createLog(taskId);
        assertNotEquals(id, otherId);
        mvc.perform(get("/tasks/{taskId}/authorization-logs", taskId).with(user("1")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.detail.length()").value(2));
        String body = "{\"task_id\":999,\"principal_id\":999,\"principal_type\":\"USER\",\"resource\":\"file\",\"action\":\"write\",\"scope\":\"mine\",\"result\":\"DENY\",\"reason\":\"권한 없음\"}";
        mvc.perform(patch("/tasks/{taskId}/authorization-logs/{id}", taskId, id).with(user("1"))
                        .contentType("application/json").content(body)).andExpect(status().isOk());
        Long agentId = jdbcTemplate.queryForObject("select AGENT_ID from AGENT_TASK where ID = ?", Long.class, taskId);
        mvc.perform(get("/tasks/{taskId}/authorization-logs/{id}", taskId, id).with(user("1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.detail.resource").value("file"))
                .andExpect(jsonPath("$.detail.action").value("write"))
                .andExpect(jsonPath("$.detail.scope").value("mine"))
                .andExpect(jsonPath("$.detail.result").value("DENY"))
                .andExpect(jsonPath("$.detail.reason").value("권한 없음"))
                .andExpect(jsonPath("$.detail.task_id").value(taskId))
                .andExpect(jsonPath("$.detail.principal_type").value("AGENT"))
                .andExpect(jsonPath("$.detail.principal_id").value(agentId));
        mvc.perform(delete("/tasks/{taskId}/authorization-logs/{id}", taskId, id).with(user("1")))
                .andExpect(status().isOk());
        mvc.perform(get("/tasks/{taskId}/authorization-logs/{id}", taskId, id).with(user("1")))
                .andExpect(status().isNotFound());
        mvc.perform(get("/tasks/{taskId}/authorization-logs/{id}", taskId, otherId).with(user("1")))
                .andExpect(status().isOk());
    }

    @Test
    void otherUsersAndWrongTasksCannotAccessLogs() throws Exception {
        long taskId = createTask();
        long id = createLog(taskId);
        String body = "{\"resource\":\"file\",\"action\":\"read\"}";
        mvc.perform(post("/tasks/{taskId}/authorization-logs", taskId).with(user("2"))
                        .contentType("application/json").content(body)).andExpect(status().isNotFound());
        mvc.perform(get("/tasks/{taskId}/authorization-logs", taskId).with(user("2")))
                .andExpect(status().isNotFound());
        mvc.perform(get("/tasks/{taskId}/authorization-logs/{id}", taskId, id).with(user("2")))
                .andExpect(status().isNotFound());
        mvc.perform(patch("/tasks/{taskId}/authorization-logs/{id}", taskId, id).with(user("2"))
                        .contentType("application/json").content(body)).andExpect(status().isNotFound());
        mvc.perform(delete("/tasks/{taskId}/authorization-logs/{id}", taskId, id).with(user("2")))
                .andExpect(status().isNotFound());
        long otherTask = createTask();
        mvc.perform(get("/tasks/{taskId}/authorization-logs/{id}", otherTask, id).with(user("1")))
                .andExpect(status().isNotFound());
        mvc.perform(patch("/tasks/{taskId}/authorization-logs/{id}", otherTask, id).with(user("1"))
                        .contentType("application/json").content(body)).andExpect(status().isNotFound());
        mvc.perform(delete("/tasks/{taskId}/authorization-logs/{id}", otherTask, id).with(user("1")))
                .andExpect(status().isNotFound());
        mvc.perform(get("/tasks/{taskId}/authorization-logs/{id}", taskId, id).with(user("1")))
                .andExpect(status().isOk());
    }

    @Test
    void validationAndLoginAreRequired() throws Exception {
        long taskId = createTask();
        long id = createLog(taskId);
        for (String body : new String[]{"{}", "{\"resource\":\" \",\"action\":\"read\"}",
                "{\"resource\":\"file\",\"action\":\"\"}",
                "{\"resource\":\"" + "x".repeat(256) + "\",\"action\":\"read\"}",
                "{\"resource\":\"file\",\"action\":\"read\",\"reason\":\"" + "x".repeat(501) + "\"}",
                "{\"resource\":\"file\",\"action\":\"read\",\"result\":\"UNKNOWN\"}"}) {
            mvc.perform(post("/tasks/{taskId}/authorization-logs", taskId).with(user("1"))
                            .contentType("application/json").content(body)).andExpect(status().isBadRequest());
            mvc.perform(patch("/tasks/{taskId}/authorization-logs/{id}", taskId, id).with(user("1"))
                            .contentType("application/json").content(body)).andExpect(status().isBadRequest());
        }
        mvc.perform(get("/tasks/{taskId}/authorization-logs", taskId))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.name").value("CUSTOM_EXPIRED_TOKEN"));
        mvc.perform(get("/tasks/{taskId}/authorization-logs/{id}", taskId, Long.MAX_VALUE).with(user("1")))
                .andExpect(status().isNotFound());
    }

    @Test
    void schemaInitializationCanRunAgainWithoutLosingLogs() throws Exception {
        long taskId = createTask();
        long id = createLog(taskId);
        schemaInitializer.afterPropertiesSet();
        schemaInitializer.afterPropertiesSet();
        mvc.perform(get("/tasks/{taskId}/authorization-logs/{id}", taskId, id).with(user("1")))
                .andExpect(status().isOk());
        // 권한 요청 처리는 호출하지 않으므로, Task 생성만으로 로그가 생기지 않는다.
        long emptyTask = createTask();
        mvc.perform(get("/tasks/{taskId}/authorization-logs", emptyTask).with(user("1")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.detail").doesNotExist());
        assertEquals(0, jdbcTemplate.queryForObject("select count(*) from AGENT_PERMISSION", Integer.class));
    }

    @Test
    void addingIdPreservesExistingRowsAndLegacyInserts() throws Exception {
        EmbeddedDatabase database = new EmbeddedDatabaseBuilder()
                .generateUniqueName(true).setType(EmbeddedDatabaseType.H2)
                .addScript("classpath:schema.sql").build();
        try {
            JdbcTemplate jdbc = new JdbcTemplate(database);
            // 기존 테이블에는 ID 없이 로그가 저장되어 있다.
            String insert = "insert into AUTHORIZATION_LOG (PRINCIPAL_TYPE, PRINCIPAL_ID, TASK_ID, RESOURCE) values ('AGENT', 1, 1, 'file')";
            jdbc.update(insert);
            InitializingBean initializer = new AuthorizationLogSchemaConfig().authorizationLogIdColumn(jdbc);
            initializer.afterPropertiesSet();
            initializer.afterPropertiesSet();
            // 기존 insert도 ID를 지정하지 않고 계속 사용할 수 있다.
            jdbc.update(insert);
            assertEquals(2, jdbc.queryForObject("select count(distinct ID) from AUTHORIZATION_LOG", Integer.class));
            assertEquals(2, jdbc.queryForObject("select count(*) from AUTHORIZATION_LOG where RESOURCE = 'file'", Integer.class));
        } finally {
            database.shutdown();
        }
    }

}
