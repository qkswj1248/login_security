package com.banban.login_security.security.risk;

import com.banban.login_security.domain.LoginAnalysisData;
import com.banban.login_security.domain.LoginHistory;
import com.banban.login_security.domain.RiskDetail;
import com.banban.login_security.mapper.LoginHistoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NewIpRule implements RiskRule{

    @Override
    public RiskDetail analyze(LoginHistory loginHistory, LoginAnalysisData data) {

        if(data.knownIps().contains(loginHistory.getIpAddress())){
            return RiskDetail.none();
        }
        return new RiskDetail("NEW_IP", 20, "새로운 ip 접속");
    }
}
