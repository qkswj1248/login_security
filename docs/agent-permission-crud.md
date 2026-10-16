# AgentPermission CRUD

AgentPermission은 에이전트의 권한 요청 기록으로 사용한다. 동일한 resource/action/scope를
다시 등록해도 매번 별도의 행과 ID가 생성된다. 기록의 존재가 권한 허용을 의미하지 않는다.

이번 구현은 기록 CRUD만 제공한다. authorize, 승인·거부 판단, 권한 요청 시 자동 저장 연동은
구현하지 않았다. 현재는 아래 POST로 기록을 생성하며, 향후 authorize 구현 시 저장 처리를 연결한다.
기존 Task와 AUTHORIZATION_LOG는 변경하지 않았다.

## API

기존 사용자 로그인 쿠키를 사용하며, 로그인한 사용자가 소유한 Agent의 기록만 관리한다.
에이전트 자체 인증은 이번 작업 범위에 포함하지 않는다.

| 메서드 | 경로 | 설명 |
| --- | --- | --- |
| POST | `/agents/{id}/permissions` | 새 권한 요청 기록 생성 |
| GET | `/agents/{id}/permissions` | 해당 Agent의 기록 목록 |
| GET | `/agents/{id}/permissions/{permissionId}` | 기록 단건 조회 |
| PATCH | `/agents/{id}/permissions/{permissionId}` | 요청 내용 수정 |
| DELETE | `/agents/{id}/permissions/{permissionId}` | 기록 삭제 |

생성과 수정은 다음 본문을 사용한다. 수정 시 resource와 action을 모두 전달한다.
scope는 생략하거나 null로 전달할 수 있으며, 수정 시 생략하면 null로 저장된다.

```json
{
  "resource": "document",
  "action": "read",
  "scope": "mine"
}
```

- resource와 action은 공백 불가, 최대 255자다. scope는 최대 255자다. 위반 시 HTTP 400이다.
- 생성 시 서버가 ID와 생성 시각을 지정한다. agent_id는 URL에서 가져온다.
- 수정 시 resource/action/scope만 바뀐다. ID, 소속 Agent, 생성 시각은 유지한다.
- 다른 사용자의 Agent, 다른 Agent의 기록, 존재하지 않는 기록에는 HTTP 404를 반환한다.
- 기존 응답 형식을 유지한다: 성공은 HTTP 200, 조회·생성 데이터는 detail에 들어간다.
  빈 목록이면 기존 Response 설정에 따라 detail이 생략된다.
- 삭제는 해당 기록 한 행만 제거한다. 승인 취소 등의 효과는 없다.

## 최소 스키마 변경

기존 AGENT_PERMISSION에는 행 식별자가 없었다. 같은 내용의 요청도 구분하여 조회·수정·삭제하도록
schema.sql과 AgentPermission에 자동 증가 ID만 추가했다. 기존 생성자도 유지했다.

새 H2 메모리 DB는 변경된 schema.sql을 사용한다. 기존 MySQL 등 영속 DB가 있고 ID 컬럼이
아직 없다면, 배포 전 해당 DB에 아래 변경을 한 번 적용해야 한다 (자동 마이그레이션은 없다).
기존 데이터가 있는 DB에 schema.sql 전체를 다시 실행하지 않는다.

```sql
ALTER TABLE AGENT_PERMISSION ADD COLUMN ID BIGINT AUTO_INCREMENT PRIMARY KEY;
```

## 코드 읽는 순서

1. AgentController의 permissions 메서드: 로그인한 사용자 ID와 경로 ID를 전달한다.
2. AgentPermissionServiceImpl: Agent 소유권과 입력을 확인한다.
3. AgentPermissionMapper와 agent_permission_mapper.xml: DB CRUD를 수행한다.
4. AgentPermissionControllerTests: 실제 H2로 반복 기록, CRUD, 접근 제한, 입력 검증을 확인한다.

## 검증 결과

AgentPermission 테스트 4개는 모두 통과했다. 전체 테스트는 13개 중 12개 통과했다.
기존 TaskControllerTests.invalidInputAndInactiveAgentAreRejected는 HTTP 400을 기대하지만
200을 받아 실패한다. 변경하지 않은 main(5bd1324)에서도 같은 실패를 재현했다.
현재 main은 Task 생성 시 만료 시각을 서버에서 10분 뒤로 설정하지만, 기존 테스트는
과거의 expired_at 입력이 거부되길 기대하고 있다. 이 작업에서는 Task 코드와 테스트를 수정하지 않았다.
따라서 전체 build 명령은 기존 Task 테스트 실패로 종료되며, 컴파일과 패키징은 성공했다.
