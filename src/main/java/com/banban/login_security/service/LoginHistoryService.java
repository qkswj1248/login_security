package com.banban.login_security.service;

import com.banban.login_security.domain.LoginHistory;

public interface LoginHistoryService {
    public void handleLoginHistory(LoginHistory loginHistory);

    public void addLoginHistory(LoginHistory loginHistory);

    public boolean isValidLoginCount(Long userId);
}
