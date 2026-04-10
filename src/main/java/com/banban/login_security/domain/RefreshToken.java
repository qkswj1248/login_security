package com.banban.login_security.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Getter @Setter
public class RefreshToken {
    private Long userId;
    private String token;
    private OffsetDateTime expires;
    private OffsetDateTime created;

    public RefreshToken(Long userId, String token, OffsetDateTime expires, OffsetDateTime created){
        this.userId = userId;
        this.token = token;
        this.expires = expires;
        this.created = created;
    }
}
