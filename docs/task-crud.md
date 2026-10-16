# Task CRUD

Task는 에이전트가 수행하는 작업 한 건이다. 같은 에이전트라도 작업을 시작할 때마다
`POST /tasks`를 호출하면 새로운 ID로 생성된다. Agent 등록 시 자동으로 생성하지 않는다.

현재는 기존 사용자 로그인 쿠키로 호출한다. 사용자 ID는 로그인 정보에서 가져오고,
요청한 `agent_id`가 해당 사용자 소유인지 확인한다. 에이전트 자체의 인증과 권한 승인 로직은
이번 구현 범위에 포함하지 않는다.

## API

| 메서드 | 경로 | 설명 |
| --- | --- | --- |
| POST | `/tasks` | 작업 시작 (새 Task 생성) |
| GET | `/tasks` | 내 작업 목록 |
| GET | `/tasks?agentId=1` | 내 특정 에이전트의 작업 목록 |
| GET | `/tasks/{taskId}` | 내 작업 단건 조회 |
| PATCH | `/tasks/{taskId}` | 작업 목적과 만료 시각 수정 |
| PATCH | `/tasks/{taskId}/status` | 작업 완료 또는 취소 |
| PATCH | `/tasks/{taskId}/cancel` | 본문 없이 작업 취소 |
| DELETE | `/tasks/{taskId}` | 작업 행 삭제 |

생성 요청 예시 (`expired_at`은 실제 호출 시각보다 미래여야 한다):

```json
{
  "agent_id": 1,
  "purpose": "사용자가 요청한 자료 검색",
  "expired_at": "2099-01-01T12:00:00+09:00"
}
```

생성 시 서버가 `id`, `user_id`, `status=RUNNING`, `created_at`을 결정한다.
비활성(`DISABLED`) 에이전트는 새 작업을 시작할 수 없다.
성공 응답은 기존 Response 형식과 HTTP 200을 사용하며, 생성·단건 조회 결과는 `detail`에 들어간다.
목록이 비어 있으면 기존 Response 설정에 따라 `detail`이 생략된다.

수정 요청은 `purpose`와 `expired_at`을 모두 전달한다. 에이전트·소유자·생성 시각은 변경하지 않는다.
상태 변경 요청은 다음과 같다.

```json
{"status": "COMPLETED"}
```

## 상태와 오류

- 시작 상태는 `RUNNING`, 종료 상태는 `COMPLETED` 또는 `CANCELLED`다.
- 종료되거나 만료된 작업의 수정·상태 변경은 HTTP 409로 거부한다. 재작업은 새 Task로 생성한다.
- 만료는 `expired_at`으로 판단한다. 자동으로 상태를 변경하는 스케줄러는 없다.
- 이름 대신 작업 목적을 검증한다: 공백 불가, 1~500자. 만료 시각은 미래여야 한다 (HTTP 400).
- 없는 작업과 다른 사용자 작업은 동일하게 HTTP 404로 처리한다.
- 삭제는 Task 행만 제거한다. 권한·승인 로그와 연동한 삭제 정책은 아직 구현하지 않았다.
- 기존 `/tasks/authorization/check` 골격은 그대로 유지했다.

## 코드 읽는 순서

1. `TaskController`: 요청을 받고 로그인한 사용자 ID를 서비스에 전달한다.
2. `AgentTaskServiceImpl`: 소유권, 입력, 상태를 검사한다.
3. `AgentTaskMapper`와 `agent_task_mapper.xml`: DB에 저장·조회·수정·삭제한다.
4. `TaskControllerTests`: 실제 H2 DB를 사용하여 CRUD, 소유권과 상태 제한을 검증한다.

기존 SQL의 `TASK_ID`, `STARTED_AT`은 실제 스키마의 `ID`, `CREATED_AT`에 맞게 수정했다.
