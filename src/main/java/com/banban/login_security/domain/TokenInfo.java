package com.banban.login_security.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Builder
@Getter
public class TokenInfo {
    private String accessToken;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String refreshToken;
    private String grantType;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private OffsetDateTime expired;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private OffsetDateTime created;

    public static TokenInfo of(String accessToken, String refreshToken, String grantType, OffsetDateTime expired, OffsetDateTime created){
        return TokenInfo.builder()
                .grantType(grantType)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expired(expired)
                .created(created)
                .build();
    }
}
