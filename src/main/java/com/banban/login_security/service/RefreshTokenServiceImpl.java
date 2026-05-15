package com.banban.login_security.service;

import com.banban.login_security.code.CommonErrorCode;
import com.banban.login_security.code.SecurityErrorCode;
import com.banban.login_security.domain.LoginWebDetails;
import com.banban.login_security.domain.RefreshToken;
import com.banban.login_security.error.CustomException;
import com.banban.login_security.mapper.RefreshTokenMapper;
import com.banban.login_security.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService{

    private final RefreshTokenMapper refreshTokenMapper;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public List<RefreshToken> getRefreshTokenForUserId(Long userId) {
        return refreshTokenMapper.findRefreshTokenForUserId(userId);
    }

    @Override
    public Optional<RefreshToken> getRefreshTokenForDeviceId(Long userId, String deviceId) {
        return refreshTokenMapper.findRefreshTokenForDeviceId(userId, deviceId);
    }

    @Override
    public void addRefreshToken(RefreshToken refreshToken) {
        try{
            refreshTokenMapper.insert(refreshToken);
        }catch (DataAccessException e){
            throw new CustomException(CommonErrorCode.DATA_ACCESS_ERROR, "RefreshToken 추가 중 문제 발생", e);
        }
    }

    @Override
    public RefreshToken createRefreshToken(Long userId, LoginWebDetails details){
        // refresh token 발급받기
        String refreshToken = jwtTokenProvider.createRefreshToken(userId.toString());
        Claims claims = jwtTokenProvider.parseClaims(refreshToken);
        OffsetDateTime expire = claims.getExpiration().toInstant().atOffset(ZoneOffset.UTC);

        // refresh 객체 준비
        return new RefreshToken(userId, refreshToken, details.agent(), details.deviceId(), expire, OffsetDateTime.now());
    }

    @Override
    public boolean isExistingDeviceId(Long userId, String deviceId){
        if(deviceId == null || deviceId.isEmpty()){
            return false;
        }else return refreshTokenMapper.findRefreshTokenForDeviceId(userId, deviceId).isPresent();
    }

    @Override
    public void loginWithNewDevice(Long userId, RefreshToken refreshToken){
        try{
            List<RefreshToken> refreshTokens = refreshTokenMapper.findRefreshTokenForUserId(userId);
            if(refreshTokens.size() < 5){
                // 새로 넣기
                refreshTokenMapper.insert(refreshToken);
            }else{
                RefreshToken oldest = refreshTokens.stream()
                        .min(Comparator.comparing(RefreshToken::getCreated))
                        .orElseThrow(() -> new CustomException(SecurityErrorCode.UNKNOWN_ERROR, "뭐징"));

                refreshTokenMapper.updateForDeviceId(refreshToken, oldest.getDeviceId());
            }
        }catch (Exception e){
            throw new CustomException(SecurityErrorCode.UNKNOWN_ERROR, "refresh token 재발급 과정 문제 발생");
        }
    }

    @Override
    public void loginWithExistingDevice(Long userId, RefreshToken refreshToken){
        int row = refreshTokenMapper.updateForDeviceId(refreshToken, refreshToken.getDeviceId());
        if(row != 1){
            throw new CustomException(SecurityErrorCode.UNKNOWN_ERROR, "refreshToken update 실패");
        }
    }

    @Override
    public String createDeviceId(){
        return UUID.randomUUID().toString();
    }
}
