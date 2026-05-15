package com.banban.login_security.domain;

public record TokenInfo(String accessToken, String refreshToken, String deviceId) {
}
