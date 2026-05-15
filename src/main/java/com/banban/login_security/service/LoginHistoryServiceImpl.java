package com.banban.login_security.service;

import com.banban.login_security.code.CommonErrorCode;
import com.banban.login_security.domain.LoginHistory;
import com.banban.login_security.error.CustomException;
import com.banban.login_security.mapper.LoginHistoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginHistoryServiceImpl implements LoginHistoryService{

    private final LoginHistoryMapper loginHistoryMapper;

    @Override
    @Async
    @EventListener
    public void handleLoginHistory(LoginHistory loginHistory) {
        try{
            addLoginHistory(loginHistory);
        }catch (Exception e){
            log.error("로그 저장 실패 사용자 : {}", loginHistory.getId());
        }
    }

    @Override
    @Transactional
    public void addLoginHistory(LoginHistory loginHistory) {
        try{
            loginHistoryMapper.insert(loginHistory);
        }catch (DataAccessException e){
            throw new CustomException(CommonErrorCode.DATA_ACCESS_ERROR, "LoginHistory 추가 중 문제 발생",e);
        }
    }

    @Override
    public boolean isValidLoginCount(Long userId) {
        int count = 0;
        try{
            count = loginHistoryMapper.countRecentLogin(userId);
        }catch (DataAccessException e){
            throw new CustomException(CommonErrorCode.DATA_ACCESS_ERROR, "로그인 횟수 확인 중 문제 발생");
        }
        return count < 5;
    }


}
