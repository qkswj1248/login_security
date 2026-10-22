package com.banban.login_security;

import com.banban.login_security.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:jwt_login_test;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "jwt.secret=and0LWxvZ2luLXRlc3Qtb25seS1rZXkteHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eA=="
})
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class JwtLoginIntegrationTests {
    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtTokenProvider tokenProvider;
    @Value("${jwt.secret}") private String testSecret;

    private String email;
    private Long userId;
    private static final String PASSWORD = "LoginTest123!";

    @BeforeEach
    void registerUserThroughApi() throws Exception {
        // 비동기 로그인 이력도 실제 DB에 저장하도록 테스트 전체를 트랜잭션으로 감싸지 않는다.
        email = "jwt-" + UUID.randomUUID() + "@example.test";
        mvc.perform(post("/users/join").contentType("application/json")
                        .content(json(Map.of("email", email, "password", PASSWORD, "name", "JWT 테스트", "provider", "LOCAL"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("CREATED_SUCCESS"));
        userId = jdbc.queryForObject("select ID from USER_INFO where EMAIL = ?", Long.class, email);
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private MvcResult login(Cookie... cookies) throws Exception {
        var request = post("/users/login").header("User-Agent", "Mozilla/5.0")
                .contentType("application/json").content(json(Map.of("email", email, "password", PASSWORD)));
        if (cookies.length > 0) request.cookie(cookies);
        return mvc.perform(request)
                .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("LOGIN_SUCCESS"))
                .andReturn();
    }

    private Cookie cookie(MvcResult result, String name) {
        Cookie value = result.getResponse().getCookie(name);
        assertNotNull(value, name + " cookie must exist");
        assertFalse(value.getValue().isBlank());
        return value;
    }

    private int refreshTokenCount() {
        return jdbc.queryForObject("select count(*) from REFRESH_TOKEN where USERID = ?", Integer.class, userId);
    }

    // 만료/탈취 시나리오는 기다리지 않고 테스트 키로 필요한 JWT를 직접 만든다.
    private String signedRefreshToken(Instant expiry) {
        return Jwts.builder().setSubject(userId.toString()).setExpiration(Date.from(expiry))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(testSecret)), SignatureAlgorithm.HS256).compact();
    }

    @Test
    void passwordIsHashedAndDuplicateSignupIsRejected() throws Exception {
        String stored = jdbc.queryForObject("select PASSWORD from USER_INFO where ID = ?", String.class, userId);
        assertNotEquals(PASSWORD, stored);
        assertTrue(passwordEncoder.matches(PASSWORD, stored));
        mvc.perform(post("/users/join").contentType("application/json")
                        .content(json(Map.of("email", email, "password", PASSWORD, "name", "중복", "provider", "LOCAL"))))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.name").value("EXISTING_USER"));
    }

    @Test
    void loginIssuesCookiesStoresRefreshTokenAndAuthenticatesProtectedApi() throws Exception {
        MvcResult result = login();
        Cookie access = cookie(result, "accessToken");
        Cookie refresh = cookie(result, "refreshToken");
        Cookie device = cookie(result, "deviceId");
        for (Cookie value : new Cookie[]{access, refresh, device}) {
            assertTrue(value.isHttpOnly());
            assertEquals("/", value.getPath());
        }
        assertEquals(userId.toString(), tokenProvider.parseClaims(access.getValue()).getSubject());
        assertEquals("BASIC", tokenProvider.parseClaims(access.getValue()).get("auth"));
        assertNull(tokenProvider.parseClaims(refresh.getValue()).get("auth"));
        assertEquals(1, refreshTokenCount());
        assertEquals(refresh.getValue(), jdbc.queryForObject(
                "select TOKEN from REFRESH_TOKEN where USERID = ? and DEVICEID = ?", String.class, userId, device.getValue()));
        mvc.perform(post("/users/auth_test").cookie(access))
                .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("AUTH_SUCCESS"));
        // JWT subject가 실제 로그인 사용자로 사용되는지도 확인한다.
        mvc.perform(post("/agents").cookie(access).contentType("application/json").content("{\"name\":\"JWT agent\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.detail.user_id").value(userId));
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> assertEquals(1,
                jdbc.queryForObject("select count(*) from LOGIN_HISTORY where USER_ID = ? and SUCCESS = true", Integer.class, userId)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"wrong-password", "unknown-user"})
    void incorrectCredentialsAreRejectedWithoutIssuingTokens(String scenario) throws Exception {
        String loginEmail = scenario.equals("unknown-user") ? "missing-" + email : email;
        String password = scenario.equals("wrong-password") ? "wrong" : PASSWORD;
        MvcResult result = mvc.perform(post("/users/login").contentType("application/json")
                        .content(json(Map.of("email", loginEmail, "password", password))))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.name").value("SECURITY_AUTH_WRONG")).andReturn();
        assertNull(result.getResponse().getCookie("accessToken"));
        assertNull(result.getResponse().getCookie("refreshToken"));
        assertEquals(0, refreshTokenCount());
        if (scenario.equals("wrong-password")) {
            await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> assertEquals(1,
                    jdbc.queryForObject("select count(*) from LOGIN_HISTORY where USER_ID = ? and SUCCESS = false", Integer.class, userId)));
        }
    }

    @Test
    void missingMalformedExpiredAndWronglySignedAccessTokensAreRejected() throws Exception {
        mvc.perform(post("/users/auth_test")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.name").value("CUSTOM_EXPIRED_TOKEN"));
        String expired = Jwts.builder().setSubject(userId.toString()).claim("auth", "BASIC")
                .setExpiration(Date.from(Instant.now().minusSeconds(60)))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(testSecret)), SignatureAlgorithm.HS256).compact();
        String wrongSignature = Jwts.builder().setSubject(userId.toString()).claim("auth", "BASIC")
                .setExpiration(Date.from(Instant.now().plusSeconds(3600)))
                .signWith(Keys.hmacShaKeyFor(new byte[64]), SignatureAlgorithm.HS256).compact();
        for (String token : new String[]{"not-a-jwt", expired, wrongSignature}) {
            mvc.perform(post("/users/auth_test").cookie(new Cookie("accessToken", token)))
                    .andExpect(status().isNotFound()).andExpect(jsonPath("$.name").value("CUSTOM_EXPIRED_TOKEN"));
        }
    }

    @Test
    void refreshTokenCannotAuthenticateProtectedApi() throws Exception {
        Cookie refresh = cookie(login(), "refreshToken");
        mvc.perform(post("/users/auth_test").cookie(new Cookie("accessToken", refresh.getValue())))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.name").value("NOT_ACCESS_TOKEN"));
    }

    @Test
    void refreshWithKnownDeviceIssuesUsableAccessToken() throws Exception {
        MvcResult loggedIn = login();
        Cookie device = cookie(loggedIn, "deviceId");
        MvcResult refreshed = mvc.perform(post("/users/auth").cookie(cookie(loggedIn, "refreshToken"), device))
                .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("AUTH_SUCCESS")).andReturn();
        Cookie access = cookie(refreshed, "accessToken");
        Cookie refresh = cookie(refreshed, "refreshToken");
        assertEquals(refresh.getValue(), jdbc.queryForObject(
                "select TOKEN from REFRESH_TOKEN where USERID = ? and DEVICEID = ?", String.class, userId, device.getValue()));
        assertEquals(1, refreshTokenCount());
        mvc.perform(post("/users/auth_test").cookie(access)).andExpect(status().isOk());
    }

    @Test
    void refreshWithMissingOrUnknownDeviceIsRejected() throws Exception {
        Cookie refresh = cookie(login(), "refreshToken");
        mvc.perform(post("/users/auth").cookie(refresh)).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.name").value("REFRESH_TOKEN_NOT_EXIST"));
        mvc.perform(post("/users/auth").cookie(refresh, new Cookie("deviceId", "unknown-device")))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.name").value("REFRESH_TOKEN_NOT_EXIST"));
        assertEquals(1, refreshTokenCount());
    }

    @Test
    void malformedAndExpiredRefreshTokensAreRejected() throws Exception {
        for (String token : new String[]{"not-a-jwt", signedRefreshToken(Instant.now().minusSeconds(60))}) {
            String error = token.equals("not-a-jwt") ? "CUSTOM_WRONG_TYPE_TOKEN" : "CUSTOM_EXPIRED_TOKEN";
            mvc.perform(post("/users/auth").cookie(new Cookie("refreshToken", token), new Cookie("deviceId", "device")))
                    .andExpect(status().isNotFound()).andExpect(jsonPath("$.name").value(error));
        }
    }

    @Test
    void differentValidRefreshTokenRevokesAllDevices() throws Exception {
        MvcResult first = login();
        login(); // 새로운 기기로 로그인한다.
        assertEquals(2, refreshTokenCount());
        String differentToken = signedRefreshToken(Instant.now().plusSeconds(7200));
        mvc.perform(post("/users/auth").cookie(new Cookie("refreshToken", differentToken), cookie(first, "deviceId")))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.name").value("STOLEN_REFRESH_TOKEN"));
        assertEquals(0, refreshTokenCount());
    }

    @Test
    void loggingInAgainWithSameDeviceKeepsSingleDeviceRecord() throws Exception {
        MvcResult first = login();
        Cookie device = cookie(first, "deviceId");
        MvcResult second = login(device);
        assertEquals(device.getValue(), cookie(second, "deviceId").getValue());
        assertEquals(1, refreshTokenCount());
    }

    @Test
    void sixthDeviceReplacesOldestDevice() throws Exception {
        Cookie firstDevice = cookie(login(), "deviceId");
        // 시각에 따라 결과가 달라지지 않도록 가장 오래된 행을 명시한다.
        jdbc.update("update REFRESH_TOKEN set CREATED = ? where USERID = ? and DEVICEID = ?",
                java.time.OffsetDateTime.now().minusDays(1), userId, firstDevice.getValue());
        for (int i = 0; i < 5; i++) login();
        assertEquals(5, refreshTokenCount());
        assertEquals(0, jdbc.queryForObject("select count(*) from REFRESH_TOKEN where USERID = ? and DEVICEID = ?",
                Integer.class, userId, firstDevice.getValue()));
    }

    @Test
    void missingRefreshCookieHasSpecificError() throws Exception {
        // 쿠키 누락과 잘못된 토큰은 구분되어야 한다.
        mvc.perform(post("/users/auth"))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.name").value("NOT_COOKIE"));
    }

}
