package com.banban.login_security.mapper;

import com.banban.login_security.domain.RefreshToken;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.security.core.parameters.P;

import java.util.List;
import java.util.Optional;

@Mapper
public interface RefreshTokenMapper {
    List<RefreshToken> findRefreshTokenForUserId(@Param("userId") Long userId);
    Optional<RefreshToken> findRefreshTokenForDeviceId(@Param("userId") Long userId, @Param("deviceId") String deviceId);
    int updateForDeviceId(@Param("ref") RefreshToken refreshToken, @Param("oldest") String oldest);
    void deleteForUserId(@Param("userId") Long userId);
    void deleteForDeviceId(@Param("userId") Long userId, @Param("deviceId") String deviceId);
    void insert(RefreshToken refreshToken);
}
