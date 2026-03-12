package com.banban.login_security.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class RefreshToken {
    private String userId;
    private String token;
    private LocalDateTime expires;
    private LocalDateTime created;
}
