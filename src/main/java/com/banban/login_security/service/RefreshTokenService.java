package com.banban.login_security.service;

import com.banban.login_security.domain.LoginWebDetails;
import com.banban.login_security.domain.RefreshToken;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenService {
    public List<RefreshToken> getRefreshTokenForUserId(Long userId);

    public Optional<RefreshToken> getRefreshTokenForDeviceId(Long userId, String deviceId);

    public void addRefreshToken(RefreshToken refreshToken);

    public RefreshToken createRefreshToken(Long userId, LoginWebDetails details);

    public boolean isExistingDeviceId(Long userId, String deviceId);

    public void loginWithNewDevice(Long userId, RefreshToken refreshToken);

    public void loginWithExistingDevice(Long userId, RefreshToken refreshToken);

    public String createDeviceId();
}
