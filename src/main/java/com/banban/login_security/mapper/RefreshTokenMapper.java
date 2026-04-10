package com.banban.login_security.mapper;

import com.banban.login_security.domain.RefreshToken;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface RefreshTokenMapper {
    Optional<RefreshToken> findRefreshToken(@Param("userId") Long userId);
    void deleteForUserId(@Param("userId") Long userId);
    void insert(RefreshToken refreshToken);
}
