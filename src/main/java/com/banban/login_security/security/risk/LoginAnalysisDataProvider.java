package com.banban.login_security.security.risk;

import com.banban.login_security.domain.LoginAnalysisData;
import com.banban.login_security.domain.LoginHistory;
import com.banban.login_security.mapper.LoginHistoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginAnalysisDataProvider {
    private final LoginHistoryMapper loginHistoryMapper;

    public LoginAnalysisData load(Long userId){
        List<LoginHistory> recentDays = loginHistoryMapper.findRecentByUserIdForDays(userId, 10);
        List<LoginHistory> recentMinutes = loginHistoryMapper.findRecentByUserIdForMinutes(userId, 10);

        Set<String> ips = recentDays.stream()
                .map(LoginHistory::getIpAddress)
                .collect(Collectors.toSet());

        Set<String> devices = recentDays.stream()
                .map(LoginHistory::getDeviceId)
                .collect(Collectors.toSet());

        long failCount = 0;
        for(LoginHistory h : recentMinutes){
            if(!h.isSuccess()) failCount++;
            if(h.isSuccess()) failCount = 0;
        }

        log.info("recentMinutes count : {}, failCount : {}", recentMinutes.size(), failCount);

        return new LoginAnalysisData(recentDays, ips, devices, failCount);
    }
}
