package com.banban.login_security.domain;

import java.util.List;

public record RiskResult(
        int totalScore,
        List<RiskDetail> details
) {
    public boolean isDangerous(){
        return totalScore >= 70;
    }
}
