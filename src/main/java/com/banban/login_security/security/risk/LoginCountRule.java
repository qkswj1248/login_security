package com.banban.login_security.security.risk;

import com.banban.login_security.domain.LoginAnalysisData;
import com.banban.login_security.domain.LoginHistory;
import com.banban.login_security.domain.RiskDetail;
import org.springframework.stereotype.Component;

@Component
public class LoginCountRule implements RiskRule{
    @Override
    public RiskDetail analyze(LoginHistory loginHistory, LoginAnalysisData data) {

        if(data.recentFailCount() >= 5){
            return new RiskDetail("MAX_TRY", 50, "로그인 연속 시도");
        }

        return RiskDetail.none();
    }
}
