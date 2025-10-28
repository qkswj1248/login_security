package com.banban.login_security.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface RefreshTokenMapper {
    Optional<String> findRefreshToken(@Param("refreshToken") String refreshToken);
    void delete(@Param("refreshToken") String refreshToken);
    void insert(@Param("refreshToken") String refreshToken);
}
