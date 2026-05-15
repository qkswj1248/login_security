package com.banban.login_security.domain;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter @Setter
public class RefreshToken{
    private Long userId;
    private String token;
    private String agent;
    private String deviceId;
    private OffsetDateTime expires;
    private OffsetDateTime created;

    public RefreshToken(Long userId, String token, String agent, String deviceId, OffsetDateTime expires, OffsetDateTime created) {
        this.userId = userId;
        this.token = token;
        this.agent = agent;
        this.deviceId = deviceId;
        this.expires = expires;
        this.created = created;
    }
}
