package com.banban.login_security.domain;

import com.banban.login_security.mapper.LoginHistoryMapper;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter @Setter
public class LoginHistory {
    private Long id;
    private Long userId;
    private String userEmail;
    private String ipAddress;
    private String userAgent;
    private String deviceId;
    private OffsetDateTime loginAt;
    private boolean success;
    private String failReason;

    public LoginHistory(Long id, Long userId, String userEmail, String ipAddress, String userAgent, String deviceId, OffsetDateTime loginAt, boolean success, String failReason) {
        this.id = id;
        this.userId = userId;
        this.userEmail = userEmail;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.deviceId = deviceId;
        this.loginAt = loginAt;
        this.success = success;
        this.failReason = failReason;
    }

    public LoginHistory(Long userId, String userEmail, String ipAddress, String userAgent, String deviceId, OffsetDateTime loginAt, boolean success, String failReason){
        this.userId = userId;
        this.userEmail = userEmail;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.deviceId = deviceId;
        this.loginAt = loginAt;
        this.success = success;
        this.failReason = failReason;
    }
}
