package com.banban.login_security.security.risk;

import com.banban.login_security.domain.LoginAnalysisData;
import com.banban.login_security.domain.LoginHistory;
import com.banban.login_security.domain.RiskDetail;

public interface RiskRule {
    RiskDetail analyze(LoginHistory loginHistory, LoginAnalysisData data);
}
