package com.banban.login_security.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Auth implements Type{
    BASIC("BASIC");
    private final String type;
}
