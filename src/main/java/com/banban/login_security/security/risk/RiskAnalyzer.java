package com.banban.login_security.security.risk;

import com.banban.login_security.domain.LoginAnalysisData;
import com.banban.login_security.domain.LoginHistory;
import com.banban.login_security.domain.RiskDetail;
import com.banban.login_security.domain.RiskResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiskAnalyzer {
    // RiskRule 은 인터페이스고 RiskDetail 은 도메인 임
    private final List<RiskRule> riskRules;

    public RiskResult analyze(LoginHistory loginHistory, LoginAnalysisData data){
        List<RiskDetail> details = riskRules.stream()
                .map(rule -> rule.analyze(loginHistory, data))
                .filter(detail -> detail.score() > 0)
                .toList();

        int totalScore = details.stream()
                .mapToInt(RiskDetail::score)
                .sum();

        return new RiskResult(totalScore, details);
    }
}
