# AuthorizationLog CRUD

AuthorizationLog는 에이전트 권한 요청의 로그를 저장한다. 같은 내용의 요청도 각각 새 ID로 저장한다.
이번 구현은 로그 CRUD만 제공하며, 권한 요청(authorize), 승인·거부 판단, 자동 기록 연동은 구현하지 않는다.
Task 생성만으로 로그가 생기지 않는다. 향후 권한 요청 처리에서 로그 저장을 연결하면 된다.
AgentPermission은 보유 권한 테이블이며 이번 코드에서 변경하지 않는다.

## API

기존 사용자 로그인 쿠키로 호출한다. 해당 Task 소유자만 관리할 수 있다.
Agent 자체 인증은 구현하지 않았다. 기존 인증·Task·권한 관련 파일은 수정하지 않았다.

| 메서드 | 경로 | 기능 |
| --- | --- | --- |
| POST | `/tasks/{taskId}/authorization-logs` | 로그 생성 |
| GET | `/tasks/{taskId}/authorization-logs` | Task의 에이전트 로그 목록 |
| GET | `/tasks/{taskId}/authorization-logs/{logId}` | 단건 조회 |
| PATCH | `/tasks/{taskId}/authorization-logs/{logId}` | 로그 내용 수정 |
| DELETE | `/tasks/{taskId}/authorization-logs/{logId}` | 로그 삭제 |

생성·수정 본문 예시:

```json
{
  "resource": "document",
  "action": "read",
  "scope": "mine",
  "result": "DENY",
  "reason": "해당 자원에 대한 권한 없음"
}
```

- resource/action은 필수이며 공백 불가, 최대 255자다.
- scope는 최대 255자, reason은 최대 500자이며 생략 또는 null이 가능하다.
- result는 생략 또는 null이 가능하고, 입력 시 ALLOW/DENY만 받는다. 서버가 결과를 판단하지 않는다.
- 생성 시 principal_type은 AGENT, principal_id는 Task의 agent_id로 서버가 설정한다.
  task_id는 URL에서 가져온다. 요청 본문의 주체와 Task ID는 무시한다.
- 수정 시 resource/action/scope/result/reason만 바뀐다. resource/action을 모두 전달해야 하며,
  생략한 선택 필드는 null로 저장된다. 소속 Task와 Agent는 바뀌지 않는다.
- 완료·만료된 Task도 소유자가 기존 로그를 관리할 수 있다. 현재 CRUD는 수동 관리 API다.
- 없는 로그, 다른 Task의 로그, 다른 사용자의 Task는 HTTP 404다. 잘못된 본문 필드는 HTTP 400이다.
- 성공은 기존 Response 형식과 HTTP 200이다. 생성·조회 데이터는 detail에 들어간다.
  빈 목록이면 기존 Response 설정에 따라 detail이 생략된다.
- 로그 수정·삭제는 보유 권한이나 Task 상태에 영향을 주지 않는다.

## 기존 파일을 수정하지 않은 방법

기존 AuthorizationLog, AuthorizationLoGMapper, AuthorizationLogService와 schema.sql은 그대로 두었다.
CRUD용 모델 AuthorizationLogEntry와 Mapper/Service/Controller를 새 파일로 추가했다.
기존 로그 저장 인터페이스는 이번 CRUD API와 별개이며, 아직 자동 기록 경로에 연결하지 않았다.

AUTHORIZATION_LOG에는 개별 로그 ID가 없으므로 새 AuthorizationLogSchemaConfig가
기존 DB 스키마 초기화 후 ID 컬럼 유무를 확인한다. 없을 때만 다음 DDL을 실행한다.
이미 ID가 있으면 다시 실행하지 않는다. 기존 로그 행과 컬럼은 삭제하지 않는다.

```sql
ALTER TABLE AUTHORIZATION_LOG ADD COLUMN ID BIGINT AUTO_INCREMENT PRIMARY KEY;
```

이는 기존 소스 파일을 유지하면서 **실제 테이블에 ID 컬럼을 추가**하는 방식이다.
H2와 MySQL의 AUTO_INCREMENT 문법을 사용하며, 초기 실행 시 DB 계정에 ALTER 권한이 필요하다.
여러 서버가 같은 DB에 동시에 처음 접속하는 배포에서는 관리자가 위 DDL을 먼저 적용한다.
MySQL 실서버 연결 검증은 수행하지 않았다. 새 H2 DB와 재초기화는 테스트한다.

## 코드 읽는 순서

1. AuthorizationLogController: 로그인 사용자와 경로 ID를 서비스에 전달한다.
2. AuthorizationLogCrudService: Task 소유권, 입력을 검사하고 로그 주체를 결정한다.
3. AuthorizationLogCrudMapper와 authorization_log_crud_mapper.xml: DB CRUD를 수행한다.
4. AuthorizationLogSchemaConfig: 개별 로그 ID를 위한 추가 초기화다.
5. AuthorizationLogControllerTests: 실제 H2에서 CRUD, 접근 제한, 입력, 반복 요청과 재초기화를 검증한다.

## 검증 결과

추가 테스트 5개 통과: CRUD와 반복 저장, 사용자/Task 접근 제한, 입력 검증,
초기화 재실행, 기존 로그 데이터 보존 및 기존 INSERT 호환성을 확인했다.
전체 테스트는 18개 중 17개 통과했다. 기존 TaskControllerTests의
invalidInputAndInactiveAgentAreRejected는 과거 만료 시각에 HTTP 400을 기대하지만
서버가 생성 시 만료 시간을 30분 뒤로 덮어써서 HTTP 200을 반환하므로 실패한다.
이 기존 실패 때문에 build 명령은 실패로 종료된다. 컴파일과 패키징은 성공했다.
기존 코드와 테스트는 변경하지 않았다.
