package com.banban.login_security.mapper;

import com.banban.login_security.domain.LoginHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

@Mapper
public interface LoginHistoryMapper {
    /*
    1. 최근 10분 로그인 시도 횟수 확인
    2. 최근 3가지 인증된 기기 확인
    3. 최근 ip 확인
     */
    void insert(LoginHistory loginHistory);
    List<LoginHistory> findRecentByUserIdForDays(@Param("userId") Long userId, @Param("times") int times);
    List<LoginHistory> findRecentByUserIdForMinutes(@Param("userId") Long userId, @Param("times") int times);
    int countRecentLogin(@Param("userId") Long userId);
}
