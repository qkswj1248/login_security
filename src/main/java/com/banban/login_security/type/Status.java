package com.banban.login_security.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Status implements Type{
    ACTIVE("ACTIVE"),
    DISABLED("DISABLED");
    private final String type;
}
