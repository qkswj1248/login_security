package com.banban.login_security.domain;

import java.util.List;
import java.util.Set;

public record LoginAnalysisData(
        List<LoginHistory> recentHistories,
        Set<String> knownIps,
        Set<String> knownDevices,
        long recentFailCount
) {}
