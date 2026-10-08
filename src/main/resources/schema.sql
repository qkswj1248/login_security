CREATE TABLE USER_INFO (
                           ID BIGINT AUTO_INCREMENT PRIMARY KEY,
                           EMAIL VARCHAR(255) NOT NULL,
                           PASSWORD VARCHAR(255) NOT NULL,
                           NAME VARCHAR(100) NOT NULL,
                           PROVIDER VARCHAR(50) NOT NULL,
                           PROVIDER_ID VARCHAR(255),
                           AUTH VARCHAR(50) NOT NULL
);

CREATE TABLE REFRESH_TOKEN (
                               USERID BIGINT NOT NULL,
                               TOKEN VARCHAR(1000) NOT NULL,
                               AGENT VARCHAR(500),
                               DEVICEID VARCHAR(255) NOT NULL,
                               EXPIRES TIMESTAMP WITH TIME ZONE NOT NULL,
                               CREATED TIMESTAMP WITH TIME ZONE NOT NULL,

                               PRIMARY KEY (USERID, DEVICEID)
);

CREATE TABLE LOGIN_HISTORY (
                               ID BIGINT AUTO_INCREMENT PRIMARY KEY,
                               USER_ID BIGINT,
                               USER_EMAIL VARCHAR(255),
                               IP_ADDRESS VARCHAR(50),
                               USER_AGENT VARCHAR(500),
                               DEVICE_ID VARCHAR(255),
                               LOGIN_AT TIMESTAMP WITH TIME ZONE NOT NULL,
                               SUCCESS BOOLEAN NOT NULL,
                               FAIL_REASON VARCHAR(255)
);

CREATE TABLE AGENT (
                       ID BIGINT AUTH_INCREMENT PRIMARY KEY,
                       USER_ID BIGINT,
                       NAME VARCHAR(255),
                       STATUS VARCHAR(255),
                       CREATED_AT TIMESTAMP WITH TIME ZONE NOT NULL,
)

CREATE TABLE AGENT_PERMISSION (
                                  AGENT_ID BIGINT NOT NULL,
                                  RESOURCE VARCHAR(255),
                                  ACTION VARCHAR(255),
                                  SCOPE VARCHAR(255),
                                  CREATED_AT TIMESTAMP WITH TIME ZONE NOT NULL,
)

CREATE TABLE AGENT_TASK (
    ID BIGINT AUTO_INCREMENT PRIMARY KEY,
    USER_ID BIGINT NOT NULL,
    AGENT_ID BIGINT NOT NULL,
    STATUS VARCHAR(255),
    PURPOSE VARCHAR(500),
    CREATED_AT TIMESTAMP WITH TIME ZONE NOT NULL,
    EXPIRED_AT TIMESTAMP WITH TIME ZONE NOT NULL
)

CREATE TABLE AUTHORIZATION_LOG (
    PRINCIPAL_TYPE VARCHAR(255) NOT NULL,
    PRINCIPAL_ID BIGINT NOT NULL,
    TASK_ID BIGINT NOT NULL,
    RESOURCE VARCHAR(255),
    ACTION VARCHAR(255),
    SCOPE VARCHAR(255),
    RESULT VARCHAR(255),
    REASON VARCHAR(500)
)