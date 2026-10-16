package com.banban.login_security.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

@Configuration
public class AuthorizationLogSchemaConfig {
    @Bean
    @DependsOnDatabaseInitialization
    public InitializingBean authorizationLogIdColumn(JdbcTemplate jdbcTemplate) {
        return () -> {
            // 기존 schema.sql을 유지하면서, 중복 로그도 구분할 수 있는 ID를 추가한다.
            // 이미 ID가 있는 DB에서는 다시 추가하지 않는다.
            boolean hasId = Boolean.TRUE.equals(jdbcTemplate.query(
                    "select * from AUTHORIZATION_LOG where 1 = 0",
                    (ResultSetExtractor<Boolean>) rs -> {
                        for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                            if ("ID".equalsIgnoreCase(rs.getMetaData().getColumnName(i))) {
                                return true;
                            }
                        }
                        return false;
                    }));
            if (!hasId) {
                jdbcTemplate.execute("alter table AUTHORIZATION_LOG add column ID BIGINT AUTO_INCREMENT PRIMARY KEY");
            }
        };
    }
}
