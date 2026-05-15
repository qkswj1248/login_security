package com.banban.login_security.domain;

public record RiskDetail(
        String ruleName,
        int score,
        String reason) {
    public static RiskDetail none(){
        return new RiskDetail("NONE", 0, "");
    }
}
