# JWT 로그인 테스트

애플리케이션 코드는 수정하지 않고 테스트만 추가했다.

## 로그인 흐름

1. `/users/join` → MemberServiceImpl: 비밀번호를 해시하여 USER_INFO에 저장한다.
2. `/users/login` → SecurityServiceImpl → AuthenticationManager → BasicUsernamePwdAuthenticationProvider:
   CustomUserDetails로 사용자를 찾고 PasswordEncoder로 비밀번호를 검증한다.
3. JwtTokenProvider가 사용자 ID를 subject로 갖는 Access/Refresh Token을 생성한다.
   RefreshTokenServiceImpl은 사용자·기기별 토큰을 REFRESH_TOKEN에 저장한다.
4. UserController가 accessToken, refreshToken, deviceId를 HttpOnly 쿠키로 반환한다.
5. 보호된 API 요청은 JwtAuthenticationFilter가 accessToken 쿠키를 검증하고 인증 정보를 설정한다.
6. `/users/auth`는 refreshToken 서명·만료, deviceId, DB의 저장 토큰을 대조한다.
   일치하면 토큰을 발급하고 갱신한다. DB 토큰과 다르면 해당 사용자의 모든 Refresh Token을 삭제한다.
7. 성공·실패 로그인 이력은 이벤트로 전달되어 비동기로 저장된다.

현재 구현은 Authorization Bearer 헤더가 아닌 쿠키를 사용한다.
오류 응답은 기존 코드에 맞춰 HTTP 404와 응답의 name을 함께 검사한다.
HTTP 상태 코드 정책을 이번 작업에서 바꾸지는 않는다.

## 추가한 파일

- JwtTokenProviderTests: Spring 없이 JWT의 subject/권한/만료, 서명 검증, 쿠키 추출을 검증한다.
- JwtLoginIntegrationTests: 실제 Spring Security 필터·서비스·MyBatis·H2를 연결한다.
  `with(user(...))`로 인증을 건너뛰지 않고 회원가입부터 로그인까지 직접 호출한다.

통합 테스트는 다음을 확인한다.

- 비밀번호 해시 저장 및 중복 가입 거부
- 로그인 쿠키 발급, Refresh Token DB 저장, Access Token의 실제 사용자 인증
- 비밀번호 오류·미등록 사용자 로그인 거부 및 토큰 미발급
- 로그인 성공·비밀번호 실패 이력의 실제 비동기 저장
- 토큰 누락·만료·형식 오류·다른 서명의 토큰 거부
- Refresh Token을 Access Token으로 사용하는 요청 거부
- 등록된 기기의 토큰 재발급과 재발급된 Access Token의 사용
- 기기 정보 누락·미등록 기기 재발급 거부
- 유효하지만 DB와 다른 Refresh Token 사용 시 모든 기기 토큰 삭제
- 동일 기기 재로그인 및 여섯 번째 기기 로그인 시 가장 오래된 기기 교체

DB는 테스트 클래스 전용 H2다. 각 테스트는 고유 이메일로 사용자를 생성한다.
비동기 이력이 커밋되지 않은 사용자 데이터에 접근하는 문제를 피하려고 테스트 전체에
@Transactional을 붙이지 않았다. 이력 저장은 고정 sleep 대신 최대 5초의 조건 대기로 확인한다.
만료 토큰은 테스트 키와 과거 exp로 생성하여 실제 만료 시간을 기다리지 않는다.
실제 설정의 JWT 키와 외부 DB 자격 증명은 사용하지 않는다.

## 실행

Java 17과 프로젝트 Gradle Wrapper를 사용한다. H2는 dev 프로필로 설정된다.

```bash
cd /workspace/login_security
bash ./gradlew --no-daemon --max-workers=2 test \
  --tests '*JwtTokenProviderTests' --tests '*JwtLoginIntegrationTests'
```

테스트 결과는 `build/reports/tests/test/index.html`과 `build/test-results/test/`에 생성된다.
일부 테스트는 현재 코드에서 발견된 문제를 드러내기 위한 회귀 테스트다.
실패를 통과로 만들기 위해 실제 검증을 비활성화하거나 애플리케이션 코드를 수정하지 않는다.

## 실행 결과

기준 커밋: main의 6b90ef8. Java 17 / Gradle 8.14.3 / H2 dev 프로필에서
`bash ./gradlew --no-daemon --max-workers=2 build`를 실행했다.

| 테스트 | 실행 | 통과 | 실패 |
| --- | ---: | ---: | ---: |
| JwtTokenProviderTests (추가) | 9 | 8 | 1 |
| JwtLoginIntegrationTests (추가) | 13 | 12 | 1 |
| 기존 테스트 | 13 | 12 | 1 |
| 전체 | 35 | 32 | 3 |

건너뛰거나 비활성화한 테스트는 없다. 컴파일·패키징은 성공했지만 테스트 실패로 build는 실패했다.

### 1. Refresh Cookie 누락 오류 분류

실패 테스트: JwtLoginIntegrationTests.missingRefreshCookieHasSpecificError

기대 응답의 name은 NOT_COOKIE지만 실제 응답은 CUSTOM_WRONG_TYPE_TOKEN이다.
CommonFunction.getCookie는 쿠키가 없으면 null 대신 빈 문자열을 반환한다.
UserController.authForJson은 null만 확인하므로 누락 검사를 통과한다.
이후 빈 문자열을 JWT로 해석하다가 잘못된 토큰으로 분류된다.
요청은 거부되지만, 의도한 쿠키 누락 처리 분기로 들어가지 않는다.

### 2. 동일 만료 시각의 Refresh Token 중복

실패 테스트: JwtTokenProviderTests.refreshTokensMustBeUniqueEvenForSameUserAndExpiry

JwtTokenProvider.createRef는 subject와 expiration만 담고 같은 키로 서명한다.
사용자와 만료 시각이 같으면 두 토큰이 완전히 같다. exp가 초 단위이므로 빠른 재발급에서
실제로 발생할 수 있다. 이 테스트는 고정된 만료 시각을 두 번 사용하여 시간 경계와 무관하게 재현한다.
현재 구현은 DB의 현재 토큰과 비교해 재사용을 감지하므로, 재발급 후 토큰이 같으면 이전 토큰을
구분할 수 없다. RTR를 보장하려면 jti 같은 고유 값을 포함해야 한다.
애플리케이션 수정은 이번 테스트 작성에 포함하지 않았다.

### 3. 기존 Task 테스트 실패

TaskControllerTests.invalidInputAndInactiveAgentAreRejected는 과거 만료 시각을 보낸 요청에
HTTP 400을 기대한다. 현재 Task 생성 코드는 요청의 만료 시각을 무시하고 서버 시각에서
30분 뒤로 설정하므로 HTTP 200을 반환한다. 이 테스트는 이번에 추가한 JWT 테스트와 별개다.

## 한계

MockMvc 기반 서버 통합 테스트이므로 실제 브라우저의 쿠키·HTTPS·SameSite 동작이나
MySQL 호환성, 다중 서버·동시 재발급 상황은 검증하지 않았다.
