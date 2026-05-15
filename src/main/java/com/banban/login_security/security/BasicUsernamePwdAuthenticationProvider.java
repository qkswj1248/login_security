package com.banban.login_security.security;

import com.banban.login_security.code.SecurityErrorCode;
import com.banban.login_security.domain.*;
import com.banban.login_security.error.CustomException;
import com.banban.login_security.security.risk.LoginAnalysisDataProvider;
import com.banban.login_security.security.risk.RiskAnalyzer;
import com.banban.login_security.service.CustomUserDetails;
import com.banban.login_security.util.CommonFunction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BasicUsernamePwdAuthenticationProvider implements AuthenticationProvider {
    private final CustomUserDetails customUserDetails;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher publisher;
    private final LoginAnalysisDataProvider loginAnalysisDataProvider;
    private final RiskAnalyzer riskAnalyzer;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        UserDetails userDetails = customUserDetails.loadUserByUsername(authentication.getName());
        String id = userDetails.getUsername();
        Long userId = Long.parseLong(id);
        String pwd = authentication.getCredentials().toString();
        String ip = "unknown";
        String agent = "unknown";
        String deviceId = "unknown";

        if(authentication.getDetails() instanceof LoginWebDetails details){
            ip = details.ip();
            agent = details.agent();
            deviceId = details.deviceId();
        }
        boolean success = passwordEncoder.matches(pwd, userDetails.getPassword());
        LoginHistory loginHistory = new LoginHistory(userId, authentication.getName(), ip, agent, deviceId, OffsetDateTime.now(), success, "");

        /* risk 정책 실행
        여기서 하는 이유는 비밀번호 5회 이상 실패하면
        추가로 캡차를 넣거나 보안적 부분을 추가해야하기 때문
        * */
        LoginAnalysisData data = loginAnalysisDataProvider.load(userId);
        RiskResult riskResult = riskAnalyzer.analyze(loginHistory, data);

        log.info("userId : {}, riskResult : {}", userId, riskResult.totalScore());

        if(riskResult.isDangerous()){
            loginHistory.setSuccess(false);
            loginHistory.setFailReason("리스크 점수가 너무 높음");
            publisher.publishEvent(loginHistory);
            throw new CustomException(SecurityErrorCode.DANGEROUS_RISK_SCORE, "리스크 점수 70 이상임");
        }

        if(success){
            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority(id));
            return new UsernamePasswordAuthenticationToken(userDetails.getUsername(), pwd, authorities);
        }else{
            publisher.publishEvent(loginHistory);
            throw new CustomException(SecurityErrorCode.SECURITY_AUTH_WRONG, CommonFunction.getClassName() + " : 비밀번호가 맞지 않습니다.");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }
}
