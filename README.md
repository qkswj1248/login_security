# Login Security with Security Score

>사용자 로그인 시 **보안 점수(Security Score)** 를 기반으로 인증 강도를 조절하는 로그인 강화 시스템입니다.

최근 인터넷 의존도 증가와 컴퓨터 기술 및 AI의 급속한 발전으로 인해 해킹 시도는 더욱 다양하고 정교해지고 있습니다.
이에 따라 사용자의 계정을 보호하기 위한 보안의 중요성은 점점 커지고 있다고 생각합니다.

따라서 이 프로젝트는 **MFA(다중 인증)** 을 적용한 로그인 인증 강화 시스템을 구현하고자 합니다.
로그인 과정에 한정되어 있지만, 결제나 거래 등 보안이 중요한 영역에서 충분히 적용이 가능하다고 생각합니다.

## 프로젝트 소개
- 기본 로그인 인증 구현 (Spring Security + JWT)
- 사용자의 로그인 이력 / 비밀번호 변경 주기 / 기기 IP / 로그인 시도 횟수 등에 따라 점수 산정
- 보안 위험 점수가 높을 경우, 추가 인증(MFA)요구
- 목표 : 로그인 과정에서 **보안 수준을 동적으로 강화**

## 개발 일정
- 1차 : 로그인 구현 (완료)
- 2차 : 보안 점수 로직 설계 및 데이터 반영 (진행 중)
- 3차 : MFA 연동 (예정)

## 기술 스택
Spring Boot, Spring Security, JWT, MySQL

## 프로젝트 구조
📁 login_security
```shell
├── *code
│   ├── Code.java
│   ├── CommonErrorCode.java
│   ├── SuccessCode.java
│   └── UserErrorCode.java
├── *controller
│   └── UserController.java
├── *domain
│   ├── LoginMember.java
│   ├── Member.java
│   └── Response.java
├── *error
│   ├── CustomException.java
│   └── GlobalException.java
├── LoginSecurityApplication.java
├── *mapper
│   └── MemberMapper.java
├── *security
│   └── SecurityConfig.java
├── *service
│   ├── MemberService.java
│   └── MemberServiceImpl.java
└── util
```
