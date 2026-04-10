package com.banban.login_security.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProviderType implements Type{
    LOCAL("LOCAL"),
    KAKAO("KAKAO"),
    NAVER("NAVER");
    private final String type;
}
