package com.banban.login_security.security;

import com.banban.login_security.code.SecurityErrorCode;
import com.banban.login_security.type.Auth;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTests {
    // 실제 설정의 키를 사용하지 않는 테스트 전용 키다.
    private final JwtTokenProvider provider = providerWithKey("a");

    private JwtTokenProvider providerWithKey(String character) {
        String key = Base64.getEncoder().encodeToString(character.repeat(64).getBytes(StandardCharsets.UTF_8));
        return new JwtTokenProvider(key);
    }

    @Test
    void accessTokenContainsUserAuthorityAndOneHourExpiry() {
        Instant before = Instant.now();
        String token = provider.createAccessToken("42", Auth.BASIC);
        Claims claims = provider.parseClaims(token);
        assertEquals("42", claims.getSubject());
        assertEquals("BASIC", claims.get("auth"));
        assertTrue(claims.getExpiration().toInstant().isAfter(before.plusSeconds(3598)));
        assertTrue(claims.getExpiration().toInstant().isBefore(Instant.now().plusSeconds(3601)));
        assertTrue(provider.validateToken(token));
    }

    @Test
    void refreshTokenHasOneDayExpiryAndNoAccessAuthority() {
        Instant before = Instant.now();
        Claims claims = provider.parseClaims(provider.createRefreshToken("42"));
        assertEquals("42", claims.getSubject());
        assertNull(claims.get("auth"));
        assertTrue(claims.getExpiration().toInstant().isAfter(before.plusSeconds(86398)));
        assertTrue(claims.getExpiration().toInstant().isBefore(Instant.now().plusSeconds(86401)));
    }

    @Test
    void accessTokenCreatesAuthenticatedPrincipal() {
        var authentication = provider.getAuthentication(provider.createAccessToken("42", Auth.BASIC));
        assertTrue(authentication.isAuthenticated());
        assertEquals("42", authentication.getName());
        assertTrue(authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("BASIC")));
    }

    @Test
    void refreshTokenCannotBeUsedAsAccessToken() {
        var exception = assertThrows(JwtAuthenticationException.class,
                () -> provider.getAuthentication(provider.createRefreshToken("42")));
        assertEquals(SecurityErrorCode.NOT_ACCESS_TOKEN, exception.getErrorCode());
    }

    @Test
    void expiredTokenIsRejectedBeyondClockSkew() {
        String token = provider.createRef("42", OffsetDateTime.now().minusMinutes(1));
        assertFalse(provider.validateToken(token));
        assertThrows(ExpiredJwtException.class, () -> provider.parseClaims(token));
    }

    @Test
    void tokenWithDifferentSignatureIsRejected() {
        String token = providerWithKey("b").createAccessToken("42", Auth.BASIC);
        assertFalse(provider.validateToken(token));
    }

    @Test
    void malformedAndEmptyTokensAreRejected() {
        assertFalse(provider.validateToken("not-a-jwt"));
        assertFalse(provider.validateToken(""));
        assertFalse(provider.validateToken(null));
    }

    @Test
    void accessTokenIsReadFromCookieRatherThanAuthorizationHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer header-token");
        assertNull(provider.resolveAccessToken(request));
        request.setCookies(new Cookie("deviceId", "device"), new Cookie("accessToken", "cookie-token"));
        assertEquals("cookie-token", provider.resolveAccessToken(request));
    }

    @Test
    void refreshTokensMustBeUniqueEvenForSameUserAndExpiry() {
        // 같은 초에 재발급하더라도 이전 토큰과 달라야 재사용을 감지할 수 있다.
        OffsetDateTime expiry = OffsetDateTime.now().plusDays(1);
        String first = provider.createRef("42", expiry);
        String second = provider.createRef("42", expiry);
        assertNotEquals(first, second, "Refresh token rotation needs a unique token value");
    }

}
